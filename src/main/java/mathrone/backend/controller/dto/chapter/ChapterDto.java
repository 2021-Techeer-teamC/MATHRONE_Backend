package mathrone.backend.controller.dto.chapter;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChapterDto {
    private String id; //03 or 02
    private String name;  //수열 or 삼각함수 등 // chapter table chapter에서 컬럼명 변경

    @Builder
    public ChapterDto(String id, String name) {
        this.id = id;
        this.name = name;
    }
}

