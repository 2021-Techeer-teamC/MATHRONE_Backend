package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import mathrone.backend.domain.WorkBookInfo;

import javax.persistence.Column;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarouselResponseDto {

    private String workbookId;

    private String workbookTitle;

    private Short year;

    private Short month;

    private String profileImg;

    private String intro;

}
