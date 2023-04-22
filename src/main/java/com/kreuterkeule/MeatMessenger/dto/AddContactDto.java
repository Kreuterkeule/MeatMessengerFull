package com.kreuterkeule.MeatMessenger.dto;

import lombok.*;

@Data
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AddContactDto {

    private String token;
    private String nickname;

}
