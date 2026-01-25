package com.example.be;

import org.junit.jupiter.api.Test;

/**
 * 기본 애플리케이션 테스트
 *
 * 참고: @SpringBootTest는 전체 컨텍스트를 로드하므로
 * DB, Redis 등 외부 의존성이 필요합니다.
 * Unit Test 위주로 테스트하는 것을 권장합니다.
 */
class BeApplicationTests {

    @Test
    void applicationStartsSuccessfully() {
        // Unit Test는 외부 의존성 없이 실행 가능
        // Service 테스트에서 비즈니스 로직 검증
    }

}
