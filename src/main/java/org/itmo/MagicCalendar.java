package org.itmo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MagicCalendar {
    // Перечисление типов встреч
    public enum MeetingType {
        WORK, PERSONAL
    }

    private static class Meeting {
        int start, end;
        MeetingType type;

        Meeting(int start, MeetingType type) {
            this.start = start;
            this.type = type;
            this.end = start + 60;
        }
    }

    private final Map<String, List<Meeting>> userMeetings = new HashMap<>();

    /**
     * Запланировать встречу для пользователя.
     *
     * @param user имя пользователя
     * @param time временной слот (например, "10:00")
     * @param type тип встречи (WORK или PERSONAL)
     * @return true, если встреча успешно запланирована, false если:
     *         - в этот временной слот уже есть встреча, и правило замены не выполняется,
     *         - лимит в 5 встреч в день уже достигнут.
     */
    public boolean scheduleMeeting(String user, String time, MeetingType type) {
        if (!isValidTime(time)) {
            return false;
        }
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        Meeting newMeeting = new Meeting(hours * 60 + minutes, type);

        List<Meeting> meetings = userMeetings.computeIfAbsent(user, k -> new ArrayList<>());

        List<Meeting> overlappingMeetings = new ArrayList<>();
        boolean hasPersonalOverlap = false;

        for (Meeting m : meetings) {
            if ((newMeeting.start >= m.start && newMeeting.start <= m.end) || (m.start >= newMeeting.start && m.start <= newMeeting.end)) {
                if (m.type == MeetingType.PERSONAL) {
                    hasPersonalOverlap = true;
                    break;
                } else {
                    overlappingMeetings.add(m);
                }
            }
        }

        if (hasPersonalOverlap) {
            return false;
        }

        int newSize = meetings.size() - overlappingMeetings.size() + 1;
        if (newSize > 5) {
            return false;
        }

        meetings.removeAll(overlappingMeetings);
        meetings.add(newMeeting);
        return true;
    }

    /**
     * Получить список всех встреч пользователя.
     *
     * @param user имя пользователя
     * @return список временных слотов, на которые запланированы встречи.
     */
    public List<String> getMeetings(String user) {
        List<Meeting> meetings = userMeetings.get(user);
        if (meetings == null) {
            return new ArrayList<>();
        }
        List<Meeting> sorted = new ArrayList<>(meetings);
        sorted.sort(Comparator.comparingInt(m -> m.start));
        List<String> result = new ArrayList<>();
        for (Meeting m : sorted) {
            int hours = m.start / 60;
            int mins = m.start % 60;
            result.add(String.format("%02d:%02d", hours, mins));
        }
        return result;
    }

    /**
     * Отменить встречу для пользователя по заданному времени.
     *
     * @param user имя пользователя
     * @param time временной слот, который нужно отменить.
     * @return true, если встреча была успешно отменена; false, если:
     *         - встреча в указанное время отсутствует,
     *         - встреча имеет тип PERSONAL (отменять можно только WORK встречу).
     */
    public boolean cancelMeeting(String user, String time) {
        List<Meeting> meetings = userMeetings.get(user);
        if (meetings == null) {
            return false;
        }

        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int targetStart = hours * 60 + minutes;
        Iterator<Meeting> it = meetings.iterator();
        while (it.hasNext()) {
            Meeting m = it.next();
            if (m.start == targetStart) {
                if (m.type == MeetingType.WORK) {
                    it.remove();
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isValidTime(String time) {
        if (time == null || time.length() != 5 || time.charAt(2) != ':') {
            return false;
        }
        try {
            String[] parts = time.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
