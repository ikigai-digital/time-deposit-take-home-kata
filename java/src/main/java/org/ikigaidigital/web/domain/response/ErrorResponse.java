package org.ikigaidigital.web.domain.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@ToString
public class ErrorResponse {

    private int status;
    private String message;
}
