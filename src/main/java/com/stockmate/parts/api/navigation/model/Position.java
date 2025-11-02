package com.stockmate.parts.api.navigation.model;

import lombok.*;

/**
 * 창고 내 위치를 나타내는 클래스
 * 위치 형식: A5-2 (라인-위치-선반층)
 * - 라인: A~E (0~4)
 * - 위치: 0~39
 * - 선반층: 1~4 (경로 계산 시 무시)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class Position {
    
    private String originalLocation; // 원본 위치 (예: "A5-2")
    private int line;     // 라인 (A=0, B=1, C=2, D=3, E=4)
    private int position; // 위치 (0~39)
    private Integer shelf; // 선반 층 (1~4, nullable for 문/포장대)
    
    private boolean isStart;  // 시작점 (문)
    private boolean isEnd;    // 종료점 (포장대)
    
    // X, Y 좌표 (Manhattan Distance 계산용)
    private int x; // position 값
    private int y; // line * 10 (라인 간 거리)
    
    /**
     * 위치 문자열을 파싱하여 Position 객체 생성
     * @param location 위치 문자열 (예: "A5-2", "문", "포장대")
     * @return Position 객체
     */
    public static Position parse(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("위치 정보가 비어있습니다.");
        }
        
        location = location.trim();
        
        // 시작점 (문)
        if (location.equals("문") || location.equalsIgnoreCase("door") || location.equalsIgnoreCase("start")) {
            return Position.builder()
                    .originalLocation(location)
                    .line(0)
                    .position(0)
                    .shelf(null)
                    .isStart(true)
                    .isEnd(false)
                    .x(0)
                    .y(0)
                    .build();
        }
        
        // 종료점 (포장대)
        if (location.equals("포장대") || location.equalsIgnoreCase("packing") || location.equalsIgnoreCase("end")) {
            // 포장대 위치는 E39 오른쪽 끝으로 가정
            return Position.builder()
                    .originalLocation(location)
                    .line(4) // E 라인
                    .position(40) // 39 다음
                    .shelf(null)
                    .isStart(false)
                    .isEnd(true)
                    .x(40)
                    .y(40) // E라인 = 4 * 10
                    .build();
        }
        
        // 일반 위치 파싱 (예: "A5-2")
        try {
            // 라인 추출 (첫 번째 문자)
            char lineChar = location.toUpperCase().charAt(0);
            if (lineChar < 'A' || lineChar > 'E') {
                throw new IllegalArgumentException("유효하지 않은 라인입니다: " + lineChar + " (A~E만 가능)");
            }
            int line = lineChar - 'A'; // A=0, B=1, ..., E=4
            
            // 나머지 부분 분리
            String remaining = location.substring(1);
            String[] parts = remaining.split("-");
            
            if (parts.length < 1) {
                throw new IllegalArgumentException("위치 형식이 올바르지 않습니다: " + location);
            }
            
            // 위치 추출
            int position = Integer.parseInt(parts[0]);
            if (position < 0 || position > 39) {
                throw new IllegalArgumentException("유효하지 않은 위치입니다: " + position + " (0~39만 가능)");
            }
            
            // 선반 층 추출 (선택사항)
            Integer shelf = null;
            if (parts.length >= 2) {
                shelf = Integer.parseInt(parts[1]);
                if (shelf < 1 || shelf > 4) {
                    throw new IllegalArgumentException("유효하지 않은 선반 층입니다: " + shelf + " (1~4만 가능)");
                }
            }
            
            return Position.builder()
                    .originalLocation(location)
                    .line(line)
                    .position(position)
                    .shelf(shelf)
                    .isStart(false)
                    .isEnd(false)
                    .x(position)
                    .y(line * 10) // 라인 간 거리를 10으로 가정
                    .build();
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("위치 형식이 올바르지 않습니다: " + location, e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("위치 형식이 올바르지 않습니다: " + location, e);
        }
    }
    
    /**
     * 두 위치 간의 Manhattan Distance 계산
     * @param other 다른 위치
     * @return Manhattan Distance
     */
    public int manhattanDistance(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }
    
    /**
     * 위치를 문자열로 표현
     * @return 위치 문자열
     */
    @Override
    public String toString() {
        if (isStart) return "문";
        if (isEnd) return "포장대";
        
        char lineChar = (char) ('A' + line);
        if (shelf != null) {
            return lineChar + String.valueOf(position) + "-" + shelf;
        } else {
            return lineChar + String.valueOf(position);
        }
    }
    
    /**
     * 간략한 위치 표현 (선반 층 제외)
     * @return 간략한 위치 문자열
     */
    public String toSimpleString() {
        if (isStart) return "문";
        if (isEnd) return "포장대";
        
        char lineChar = (char) ('A' + line);
        return lineChar + String.valueOf(position);
    }
}

