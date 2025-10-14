package com.stockmate.parts.common.config.swagger.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityUser {

    private Long memberId;
    private Role role;

}
