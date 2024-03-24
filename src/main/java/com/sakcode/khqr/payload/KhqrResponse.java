package com.sakcode.khqr.payload;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class KhqrResponse {
    private String status;
    private String imageBase64;
    private String pdfBase64;

}
