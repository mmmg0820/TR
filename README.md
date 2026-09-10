# 먕타로 (TR)

먕타로는 질문 작성, 스프레드 선택, 카드 선택, 결과 확인과 기록 관리를 제공하는 Android 타로 앱입니다. Kotlin과 Jetpack Compose로 작성되어 있으며 Android 12(API 31) 이상을 대상으로 합니다.

## 주요 기능

- 질문 입력과 스프레드 선택
- 유니버셜 타로 78장 카드 선택
- 정방향·역방향 리딩
- 스프레드별 결과 배치
- 중단된 리딩 임시 저장 및 7일 이내 복원
- 리딩 스냅샷 저장과 기록 검색, 고정, 삭제
- 카드와 덱 데이터의 로컬 관리 기반
- 백업·복원과 개인정보를 제외한 진단 자료 내보내기

## 프로젝트 구성

- `app/src/main/java`: 앱 화면, 저장소 및 도메인 로직
- `app/src/main/assets/tarot_cards_78.json`: 78장 카드 데이터
- `app/src/main/res/drawable-nodpi`: 카드 및 앱 이미지 자산
- `app/src/test`: JVM 테스트
- `app/src/androidTest`: Android 계측 테스트

## 개발 환경

- JDK 17
- Android Studio 최신 안정 버전
- Android SDK 35 이상

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

`local.properties`는 저장소에 포함하지 않습니다. Android Studio가 로컬 SDK 경로에 맞게 생성합니다.

## 현재 상태

현재 소스는 `testDebugUnitTest`와 `assembleDebug`를 통과했습니다. Galaxy 실기기 설치·실행 스모크에서 앱 포커스, 홈 하단 CTA 간격(9dp), 치명적 크래시 로그 0건을 확인했습니다.

GitHub 업로드가 Play Production 출시 승인을 의미하지는 않습니다. Play 제출 전에는 서명된 Release AAB와 동일 SHA 빌드로 내부 테스트 및 스토어 설정 검증을 별도로 완료해야 합니다.

## 데이터와 자산

- 카드 데이터는 JSON 자산과 로컬 저장 계층을 사용합니다.
- 카드 이미지 및 의미 데이터의 배포·상업 이용 권리는 출시 전에 최종 확인해야 합니다.
- 앱에서 사용자가 수정하는 데이터는 기기 내부 저장소에 보관됩니다.

## 보안과 개인정보

- 서명키, API 키, Play Console 자격 증명 및 `local.properties`는 포함하지 않습니다.
- 실제 사용자 기록과 기기 QA 덤프는 포함하지 않습니다.
- 공개 저장소에 비밀정보를 커밋하지 마세요.

## 라이선스

별도 라이선스 파일이 추가되기 전까지 코드와 자산의 사용 권한은 저장소 소유자에게 있습니다. 포함된 데이터와 이미지의 재배포 조건은 별도로 확인해야 합니다.
