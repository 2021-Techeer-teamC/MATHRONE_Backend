package mathrone.backend.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class LevelInfoDto{
    int mostSelected;
    int userSelected;
    public LevelInfoDto(int most){
        this.mostSelected = most;
    }
    public LevelInfoDto(int most, int user){
        this.mostSelected = most;
        this.userSelected = user;
    }
}
