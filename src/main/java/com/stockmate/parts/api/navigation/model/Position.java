package com.stockmate.parts.api.navigation.model;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * 창고 내 위치를 나타내는 클래스
 * 
 * 위치 형식: A16-2 (라인-위치-선반층)
 * - 라인: A~E (0~4)
 * - 위치: 0~39 (블록당 10칸 × 4블록)
 * - 선반층: 1~4 (경로 계산 시 무시)
 * 
 * 창고 레이아웃:
 * - 각 블록 = 10칸 (윗줄 0~4, 아랫줄 5~9)
 * - 블록 사이 통로 = 1칸
 * - 물리적 블록 간격 = 6칸 (5칸 선반 + 1칸 통로)
 * 
 * 예시:
 * A0  → 블록0, 윗줄 0번 → 물리적 좌표 (0, 0)
 * A4  → 블록0, 윗줄 4번 → 물리적 좌표 (4, 0)
 * A5  → 블록0, 아랫줄 0번 → 물리적 좌표 (0, 1)
 * A10 → 블록1, 윗줄 0번 → 물리적 좌표 (6, 0)
 * A16 → 블록1, 아랫줄 1번 → 물리적 좌표 (7, 1)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@Slf4j
public class Position {
    
    private String originalLocation; // 원본 위치 (예: "A16-2")
    private int line;     // 라인 (A=0, B=1, C=2, D=3, E=4)
    private int position; // 논리적 위치 (0~39)
    private Integer shelf; // 선반 층 (1~4, nullable for 문/포장대)
    
    private boolean isStart;  // 시작점 (문)
    private boolean isEnd;    // 종료점 (포장대)
    
    // 물리적 좌표 (블록 경계 반영)
    private int x; // 물리적 가로 좌표 (블록 통로 포함)
    private int y; // 물리적 세로 좌표 (라인 × 10 + 줄)
    private int blockNumber; // 블록 번호 (0~3)
    private int row; // 줄 (0: 윗줄, 1: 아랫줄)
    
    /**
     * 위치 문자열을 파싱하여 Position 객체 생성
     * @param location 위치 문자열 (예: "A16-2", "문", "포장대")
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
                    .blockNumber(-1) // 특수 위치
                    .row(0)
                    .build();
        }
        
        // 종료점 (포장대)
        if (location.equals("포장대") || location.equalsIgnoreCase("packing") || location.equalsIgnoreCase("end")) {
            // 포장대 위치는 E39 오른쪽 끝 아래로 가정
            return Position.builder()
                    .originalLocation(location)
                    .line(4) // E 라인
                    .position(39)
                    .shelf(null)
                    .isStart(false)
                    .isEnd(true)
                    .x(30) // 블록3 끝 (3 × 6 + 4 = 22) + 약간 여유
                    .y(41) // E라인 아랫줄 (4 × 10 + 1)
                    .blockNumber(-1) // 특수 위치
                    .row(1)
                    .build();
        }
        
        // 일반 위치 파싱 (예: "A16-2")
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
            
            // 블록 기반 물리적 좌표 계산
            int blockNum = position / 10; // 블록 번호 (0~3)
            int inBlockPos = position % 10; // 블록 내 위치 (0~9)
            int row = (inBlockPos >= 5) ? 1 : 0; // 0: 윗줄 (0~4), 1: 아랫줄 (5~9)
            int col = inBlockPos % 5; // 블록 내 열 (0~4)
            
            // 물리적 x 좌표 = 블록번호 × 6 + 열
            int physicalX = blockNum * 6 + col;
            // 물리적 y 좌표 = 라인 × 10 + 줄
            int physicalY = line * 10 + row;
            
            log.debug("위치 파싱: {} → 블록={}, 줄={}, 열={}, 물리좌표=({}, {})", 
                    location, blockNum, row, col, physicalX, physicalY);
            
            return Position.builder()
                    .originalLocation(location)
                    .line(line)
                    .position(position)
                    .shelf(shelf)
                    .isStart(false)
                    .isEnd(false)
                    .x(physicalX)
                    .y(physicalY)
                    .blockNumber(blockNum)
                    .row(row)
                    .build();
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("위치 형식이 올바르지 않습니다: " + location, e);
        } catch (StringIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("위치 형식이 올바르지 않습니다: " + location, e);
        }
    }
    
    /**
     * 실제 창고 레이아웃을 반영한 거리 계산
     * 
     * 거리 계산 규칙:
     * 1. 기본: Manhattan Distance (|x2-x1| + |y2-y1|)
     * 2. 같은 블록 내 줄 전환: +4 (블록 끝까지 가서 돌아와야 함)
     * 3. 다른 블록 + 줄 전환: +2 (통로에서 줄 전환 추가 비용)
     * 
     * 예시:
     * - A0 → A1: 1칸 (같은 줄, 인접)
     * - A0 → A4: 4칸 (같은 줄)
     * - A0 → A10: 6칸 (다른 블록, 같은 줄)
     * - A0 → A5: 5칸 (같은 블록, 줄 전환, 1 + 4)
     * - A9 → A10: 5칸 (다른 블록, 줄 전환, 3 + 2)
     * - A14 → A15: 9칸 (같은 블록, 줄 전환, 5 + 4)
     * - A0 → A16: 10칸 (다른 블록, 줄 전환, 8 + 2)
     * 
     * @param other 다른 위치
     * @return 실제 이동 거리
     */
    public int manhattanDistance(Position other) {
        // 같은 라인(A, B, C, D, E)인지 확인
        int thisLineNum = this.y / 10;
        int otherLineNum = other.y / 10;
        
        if (thisLineNum != otherLineNum) {
            // 다른 라인 간 이동 (A → B 등): 라인 간 거리가 큼
            // 단순 Manhattan Distance로 처리
            int dist = Math.abs(other.x - this.x) + Math.abs(other.y - this.y);
            log.debug("다른 라인 간 이동: {} → {}, 거리={}", 
                    this.originalLocation, other.originalLocation, dist);
            return dist;
        }
        
        // 같은 라인 내에서의 이동
        int baseDistance = Math.abs(other.x - this.x) + Math.abs(other.y - this.y);
        
        // 특수 위치 (문, 포장대)는 보정 없음
        if (this.blockNumber == -1 || other.blockNumber == -1) {
            log.debug("특수 위치 이동: {} → {}, 거리={}", 
                    this.originalLocation, other.originalLocation, baseDistance);
            return baseDistance;
        }
        
        // 줄이 같으면 보정 없음 (수평 이동만)
        int thisRow = this.y % 10;
        int otherRow = other.y % 10;
        
        if (thisRow == otherRow) {
            log.debug("같은 줄 이동: {} → {}, 거리={}", 
                    this.originalLocation, other.originalLocation, baseDistance);
            return baseDistance;
        }
        
        // 줄이 다른 경우: 페널티 적용
        if (this.blockNumber == other.blockNumber) {
            // 케이스 1: 같은 블록 내 줄 전환
            // 블록 끝까지 가서 돌아와야 하므로 +4
            int finalDistance = baseDistance + 4;
            log.debug("같은 블록 내 줄 전환: {} (블록 {}) → {} (블록 {}), 기본={}, 최종={}", 
                    this.originalLocation, this.blockNumber, 
                    other.originalLocation, other.blockNumber, 
                    baseDistance, finalDistance);
            return finalDistance;
        } else {
            // 케이스 2: 다른 블록 + 줄 전환
            // 통로에서 줄 전환 시 추가 비용 +2
            int finalDistance = baseDistance + 2;
            log.debug("다른 블록 + 줄 전환: {} (블록 {}) → {} (블록 {}), 기본={}, 최종={}", 
                    this.originalLocation, this.blockNumber, 
                    other.originalLocation, other.blockNumber, 
                    baseDistance, finalDistance);
            return finalDistance;
        }
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
