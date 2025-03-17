package br.app.moments.api.messages;

public class MessageResponse {
    private int code;
    private String message;

    public MessageResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    // Getters e Setters (ou use Lombok para gerá-los automaticamente)
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
