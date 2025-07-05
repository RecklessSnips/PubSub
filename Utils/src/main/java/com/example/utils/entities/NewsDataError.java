package com.example.utils.entities;

public class NewsDataError {

    private String status;
    private Results results;

    NewsDataError() {}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Results getResults() {
        return results;
    }

    public void setResults(Results results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "NewsDataError{" +
                "status='" + status + '\'' +
                ", results=" + results +
                '}';
    }

    public static class Results {
        private String message;
        private String code;

        Results() {}

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        @Override
        public String toString() {
            return "Results{" +
                    "message='" + message + '\'' +
                    ", code='" + code + '\'' +
                    '}';
        }
    }
}
