package com.sstu.LearningManagementSystem.model.dto.userDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserUpdateDto {
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
}