package org.gkdis6;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Module {
    private String DOMAIN;
    private String CNTS_CRTC_KEY;

    public Module(String DOMAIN, String CNTS_CRTC_KEY) {
        if (DOMAIN == null || DOMAIN.isEmpty()) {
            throw new IllegalArgumentException("Domain is not provided or is empty.");
        }
        if (CNTS_CRTC_KEY == null || CNTS_CRTC_KEY.isEmpty()) {
            throw new IllegalArgumentException("CNTS_CRTC_KEY is not provided or is empty.");
        }
        this.DOMAIN = DOMAIN;
        this.CNTS_CRTC_KEY = CNTS_CRTC_KEY;
    }

    public ApiResponse sendBotNotification(String BOT_ID, String RCVR_USER_ID, String CNTN) {
        if (RCVR_USER_ID == null || RCVR_USER_ID.isEmpty()) {
            return new ApiResponse(false, 400, "RCVR_USER_ID is not provided",
                    new ApiError("Bad Request", "The RCVR_USER_ID is empty."));
        }
        if (BOT_ID == null || BOT_ID.isEmpty()) {
            return new ApiResponse(false, 400, "BOT_ID is not provided",
                    new ApiError("Bad Request", "The BOT_ID is empty or null."));
        }

        JSONObject inputData = new JSONObject();
        inputData.put("API_KEY", "FLOW_BOT_NOTI_API");
        inputData.put("CNTS_CRTC_KEY", CNTS_CRTC_KEY);

        JSONObject reqData = new JSONObject();
        reqData.put("BOT_ID", BOT_ID);
        reqData.put("RCVR_USER_ID", RCVR_USER_ID);
        reqData.put("CNTN", CNTN);

        inputData.put("REQ_DATA", reqData);

        return sendApi(inputData);
    }

    private ApiResponse sendApi(JSONObject inputData) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(DOMAIN + "/OpenGate");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);

            // 요청 본문 데이터 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = inputData.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
            int statusCode = connection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_OK) {
                // 응답 데이터 읽기
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // JSON 응답 처리
                    JSONObject responseObject = new JSONObject(response.toString());
                    if ("0000".equals(responseObject.optString("RSLT_CD"))) {
                        return new ApiResponse(true, 200, "Success", null);
                    } else {
                        return new ApiResponse(false, 400, "Error",
                                new ApiError("API Error", responseObject.optString("RSLT_MSG", "Unknown error")));
                    }
                }
            } else {
                return new ApiResponse(false, statusCode, "HTTP Error",
                        new ApiError("HTTP Error", "HTTP status code: " + statusCode));
            }
        } catch (MalformedURLException e) {
            return new ApiResponse(false, 400, "Invalid URL",
                    new ApiError("MalformedURLException", e.getMessage()));
        } catch (IOException e) {
            return new ApiResponse(false, 500, "IO Error",
                    new ApiError("IOException", e.getMessage()));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public class ApiResponse {
        public boolean success;
        public int code;
        public String message;
        public ApiError error;

        public ApiResponse(boolean success, int code, String message, ApiError error) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.error = error;
        }
    }

    public class ApiError {
        public String code;
        public String message;

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}