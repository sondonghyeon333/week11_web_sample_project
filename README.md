## STEP 2. Source Code Exploration & Analysis

## ① Source Code Analysis

Sample Project의 소스코드를 읽고 다음 8개 질문에 대한 답을 README.md에 자신의 말로 작성합니다.

### Project Structure & Spring MVC

**1. PostController, PostService, PostRepository는 각각 어떤 역할을 하며, 서로 어떻게 연결되는가?**

PostController는 사용자의 요청을 받고 화면을 연결하는 역할을 한다. PostService는 게시글을 저장하거나 수정, 삭제하는 기능을 처리한다. PostRepository는 JPA를 이용해 데이터베이스에 게시글 정보를 저장하고 조회한다. Controller가 Service를 호출하고 Service가 Repository를 사용하는 구조이다.

- 관련 파일 및 메서드: `PostController.java` - `create()`, `PostService.java` - `create()`, `PostRepository.java` - `saveAndFlush()`(상속 메서드)

**2. 게시글 작성 시 HTML Form의 입력값과 첨부파일은 Controller에 어떻게 전달되는가?**

HTML Form은 `multipart/form-data` 방식으로 입력값과 파일을 함께 보낸다. Controller에서는 `@ModelAttribute`로 제목과 내용을 `PostForm`에 받고, `@RequestParam`으로 첨부파일을 `MultipartFile` 목록에 받는다. 받은 내용과 파일은 Service로 전달된다.

- 관련 파일 및 메서드: `form.html` - 게시글 작성 Form, `PostController.java` - `create()`

### JPA & Entity Relationship

**1. Post와 Attachment Entity는 어떤 관계이며, @ManyToOne과 @OneToMany는 각각 어떤 역할을 하는가?**

게시글 하나에 첨부파일을 여러 개 넣을 수 있어서 일대다 관계이다. `@OneToMany`는 하나의 Post에 여러 Attachment가 연결되는 것을 나타낸다. `@ManyToOne`은 여러 Attachment가 하나의 Post에 속한다는 것을 나타낸다.

- 관련 파일 및 메서드: `Post.java` - `addAttachment()`, `Attachment.java` - `setPost()`

**2. cascade = CascadeType.ALL과 orphanRemoval = true는 어떤 역할을 하며, 실제 디스크에 저장된 파일에도 영향을 주는가?**

`CascadeType.ALL`은 게시글을 저장하거나 삭제할 때 연결된 첨부파일 정보에도 JPA 작업이 적용되게 한다. `orphanRemoval = true`는 게시글과 연결이 끊어진 첨부파일 정보를 DB에서 삭제하도록 한다. 하지만 실제 디스크에 저장된 파일은 자동으로 삭제되지 않기 때문에 따로 삭제해야 한다.

- 관련 파일 및 메서드: `Post.java` - `attachments` 관계 설정, `PostService.java` - `delete()`, `FileStorageService.java` - `deleteIfExists()`

### File Upload

**1. MultipartFile은 무엇이며, 업로드된 파일은 어떤 과정을 거쳐 서버에 저장되는가?**

`MultipartFile`은 사용자가 업로드한 파일을 Spring에서 처리할 때 사용하는 것이다. Controller가 파일을 받으면 Service에서 파일 이름과 형식, 크기를 검사한다. 문제가 없으면 서버의 `uploads` 폴더에 저장하고 파일 정보는 DB에 저장한다.

- 관련 파일 및 메서드: `PostController.java` - `create()`, `PostService.java` - `create()`, `FileStorageService.java` - `store()`

**2. 파일을 원본 이름 대신 UUID 기반 파일명으로 저장하는 이유는 무엇인가?**

서로 다른 사용자가 같은 이름의 파일을 올릴 수도 있기 때문이다. UUID를 사용하면 저장할 때 파일 이름이 겹치는 문제를 줄일 수 있다. 원래 파일 이름은 따로 보관해서 다운로드할 때 사용한다.

- 관련 파일 및 메서드: `FileStorageService.java` - `store()`, `Attachment.java` - `getOriginalFilename()`, `getStoredFilename()`

### File Download & Security

**1. 파일 다운로드 시 Resource, ResponseEntity, Content-Disposition은 각각 어떤 역할을 하는가?**

`Resource`는 서버에 저장된 파일을 읽어 전달할 때 사용한다. `ResponseEntity`는 파일 데이터와 HTTP 응답 정보를 함께 보내는 역할을 한다. `Content-Disposition`은 파일을 다운로드하도록 하고 원래 파일 이름을 지정한다.

- 관련 파일 및 메서드: `AttachmentController.java` - `download()`, `FileStorageService.java` - `load()`

**2. 파일 저장 과정에서 normalize()와 startsWith()를 사용하는 이유는 무엇인가?**

`normalize()`는 파일 경로를 정리하는 역할을 한다. `startsWith()`는 저장하려는 경로가 정해진 업로드 폴더 안에 있는지 확인한다. 이를 통해 다른 폴더에 잘못 접근하는 문제를 막을 수 있다.

- 관련 파일 및 메서드: `FileStorageService.java` - `store()`, `load()`


## STEP 3. Customizing

### 1. Text 변경

- 수정 파일: `src/main/resources/templates/posts/list.html`
- 변경 전: 자료 공유 게시판
- 변경 후: 나의 파일 게시판

게시판 제목을 변경했다.

### 2. Style 변경

- 수정 파일: `src/main/resources/static/css/app.css`
- 변경 전: 배경색 `#f5f7fb`
- 변경 후: 배경색 `#eaf4ff`

게시판의 배경색을 연한 하늘색으로 변경했다.

### 3. UI/Data 변경

- 수정 파일: `src/main/resources/templates/posts/detail.html`
- 변경 전: 파일명과 파일 크기 표시
- 변경 후: 파일명, 파일 크기, 파일 타입 표시

Thymeleaf의 `file.contentType`을 사용하여
첨부파일의 종류도 표시하도록 수정했다.
