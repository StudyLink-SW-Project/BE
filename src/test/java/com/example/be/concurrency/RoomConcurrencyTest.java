package com.example.be.concurrency;

import com.example.be.domain.Room;
import com.example.be.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 스터디룸 참여자 수 동시성 테스트
 *
 * 비관적 락(Pessimistic Lock)을 사용하여 동시에 여러 사용자가
 * 방에 입장할 때 참여자 수의 정합성이 보장되는지 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
class RoomConcurrencyTest {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Room testRoom;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        roomRepository.deleteAll();

        // 테스트용 방 생성
        testRoom = Room.builder()
                .title("동시성테스트방")
                .description("동시 입장 테스트")
                .maxParticipants(100)
                .participantCount(0)
                .createDate(System.currentTimeMillis())
                .build();
        roomRepository.save(testRoom);
    }

    @Test
    @DisplayName("동시에 10명이 입장해도 참여자 수가 정확히 10이 되어야 한다")
    void concurrentJoin_WithPessimisticLock_ShouldMaintainCorrectCount() throws InterruptedException {
        // Given
        int threadCount = 10;  // 동시 입장 인원
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // When: 10명이 동시에 입장 시도
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        // 비관적 락을 사용하여 방 조회
                        Room room = roomRepository.findByTitleWithLock("동시성테스트방");
                        if (room != null) {
                            room.setParticipantCount(room.getParticipantCount() + 1);
                            roomRepository.save(room);
                            successCount.incrementAndGet();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // 모든 스레드 완료 대기
        latch.await();
        executorService.shutdown();

        // Then: 참여자 수가 정확히 10이어야 함
        Room result = roomRepository.findByTitle("동시성테스트방");

        System.out.println("========== 동시성 테스트 결과 ==========");
        System.out.println("동시 요청 수: " + threadCount);
        System.out.println("성공 횟수: " + successCount.get());
        System.out.println("실패 횟수: " + failCount.get());
        System.out.println("최종 참여자 수: " + result.getParticipantCount());
        System.out.println("=======================================");

        assertThat(result.getParticipantCount()).isEqualTo(threadCount);
        assertThat(successCount.get()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시에 50명이 입장해도 참여자 수가 정확히 50이 되어야 한다")
    void concurrentJoin_50Users_ShouldMaintainCorrectCount() throws InterruptedException {
        // Given
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        Room room = roomRepository.findByTitleWithLock("동시성테스트방");
                        if (room != null) {
                            room.setParticipantCount(room.getParticipantCount() + 1);
                            roomRepository.save(room);
                            successCount.incrementAndGet();
                        }
                        return null;
                    });
                } catch (Exception e) {
                    System.err.println("오류 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        Room result = roomRepository.findByTitle("동시성테스트방");

        System.out.println("========== 50명 동시 입장 테스트 ==========");
        System.out.println("최종 참여자 수: " + result.getParticipantCount());
        System.out.println("==========================================");

        assertThat(result.getParticipantCount()).isEqualTo(threadCount);
    }

    @Test
    @DisplayName("동시 입장/퇴장 시에도 참여자 수가 정확해야 한다")
    void concurrentJoinAndLeave_ShouldMaintainCorrectCount() throws InterruptedException {
        // Given: 먼저 10명이 입장한 상태로 설정
        transactionTemplate.execute(status -> {
            Room room = roomRepository.findByTitle("동시성테스트방");
            room.setParticipantCount(10);
            roomRepository.save(room);
            return null;
        });

        int joinCount = 5;   // 입장할 인원
        int leaveCount = 3;  // 퇴장할 인원
        int totalThreads = joinCount + leaveCount;

        ExecutorService executorService = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        // When: 5명 입장 + 3명 퇴장 동시 실행
        // 입장 스레드
        for (int i = 0; i < joinCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        Room room = roomRepository.findByTitleWithLock("동시성테스트방");
                        if (room != null) {
                            room.setParticipantCount(room.getParticipantCount() + 1);
                            roomRepository.save(room);
                        }
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        // 퇴장 스레드
        for (int i = 0; i < leaveCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        Room room = roomRepository.findByTitleWithLock("동시성테스트방");
                        if (room != null && room.getParticipantCount() > 0) {
                            room.setParticipantCount(room.getParticipantCount() - 1);
                            roomRepository.save(room);
                        }
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then: 10 + 5 - 3 = 12명이어야 함
        Room result = roomRepository.findByTitle("동시성테스트방");
        int expectedCount = 10 + joinCount - leaveCount;

        System.out.println("========== 동시 입장/퇴장 테스트 ==========");
        System.out.println("초기 인원: 10");
        System.out.println("입장 인원: " + joinCount);
        System.out.println("퇴장 인원: " + leaveCount);
        System.out.println("예상 인원: " + expectedCount);
        System.out.println("실제 인원: " + result.getParticipantCount());
        System.out.println("==========================================");

        assertThat(result.getParticipantCount()).isEqualTo(expectedCount);
    }

    @Test
    @DisplayName("최대 인원 초과 시 입장이 제한되어야 한다")
    void concurrentJoin_ShouldNotExceedMaxParticipants() throws InterruptedException {
        // Given: 최대 인원 10명인 방
        transactionTemplate.execute(status -> {
            Room room = roomRepository.findByTitle("동시성테스트방");
            room.setMaxParticipants(10);
            room.setParticipantCount(8);  // 이미 8명 있음
            roomRepository.save(room);
            return null;
        });

        int threadCount = 5;  // 5명이 동시에 입장 시도 (2명만 성공해야 함)
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    transactionTemplate.execute(status -> {
                        Room room = roomRepository.findByTitleWithLock("동시성테스트방");
                        if (room != null) {
                            if (room.getParticipantCount() < room.getMaxParticipants()) {
                                room.setParticipantCount(room.getParticipantCount() + 1);
                                roomRepository.save(room);
                                successCount.incrementAndGet();
                            } else {
                                rejectedCount.incrementAndGet();
                            }
                        }
                        return null;
                    });
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // Then
        Room result = roomRepository.findByTitle("동시성테스트방");

        System.out.println("========== 최대 인원 제한 테스트 ==========");
        System.out.println("최대 인원: 10");
        System.out.println("초기 인원: 8");
        System.out.println("입장 시도: " + threadCount);
        System.out.println("입장 성공: " + successCount.get());
        System.out.println("입장 거부: " + rejectedCount.get());
        System.out.println("최종 인원: " + result.getParticipantCount());
        System.out.println("==========================================");

        // 최대 인원(10)을 초과하지 않아야 함
        assertThat(result.getParticipantCount()).isLessThanOrEqualTo(10);
        // 정확히 10명이어야 함 (8 + 2)
        assertThat(result.getParticipantCount()).isEqualTo(10);
        // 2명만 성공, 3명은 거부되어야 함
        assertThat(successCount.get()).isEqualTo(2);
        assertThat(rejectedCount.get()).isEqualTo(3);
    }
}
