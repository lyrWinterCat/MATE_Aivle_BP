package com.example.MATE.dto;
import com.example.MATE.model.Meeting;
import com.example.MATE.model.Summary;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SummaryDto {

    private Integer summaryId;
    private Integer meetingId;
    private String summaryTopic;
    private String summaryPositiveNegative;
    private String todoList;
    private String summaryTotal;
    private String summarySharedFile;

    // ✅ 엔티티 → DTO 변환
    public static SummaryDto fromEntity(Summary summary) {
        return new SummaryDto(
                summary.getSummaryId(),
                summary.getMeeting().getMeetingId(), // Meeting 객체 대신 meetingId 저장
                summary.getSummaryTopic(),
                summary.getSummaryPositiveNegative(),
                summary.getTodoList(),
                summary.getSummaryTotal(),
                summary.getSummarySharedFile()
        );
    }

    // ✅ DTO → 엔티티 변환 (데이터 저장 시 사용)
    public Summary toEntity(Meeting meeting) {
        Summary summary = new Summary();
        summary.setSummaryId(this.summaryId);
        summary.setMeeting(meeting);
        summary.setSummaryTopic(this.summaryTopic);
        summary.setSummaryPositiveNegative(this.summaryPositiveNegative);
        summary.setTodoList(this.todoList);
        summary.setSummaryTotal(this.summaryTotal);
        summary.setSummarySharedFile(this.summarySharedFile);
        return summary;
    }
}