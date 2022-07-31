/*
* databaseManager
* 데이터베이스 관련된 클래스
* asfdasdf
* */

package metro_navi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

class databaseManager {

    timeAndDate time = new timeAndDate();
    private static Connection conn = null;

    /*conn 생성*/
    public static void connectDatabase() {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306/?user=root";
        String userName = "root";
        String password = "19980316";
        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(dbURL, userName, password);
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
    }

    /*public static ArrayList getStationDataWithNameDB(String stationName)
    * 역 이름으로 해당 역과 관련된 정보를 가져옴
    * 정보가 여러개면 ArrayList에 담아서 전달
    * */
    public static ArrayList getStationDataWithNameDB(String stationName) {
        ArrayList<subwayData> station = new ArrayList<subwayData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery1;
            strQuery1 = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, "
                    + "next_station, " + "station_id FROM Subway.sub_line_name_info WHERE station_name = \"%s\" AND city_id = 1000", stationName);
            java.sql.ResultSet resultSet1 = stmt.executeQuery(strQuery1);
            while(resultSet1.next()) {
                subwayData temp = new subwayData();
                temp.stationName = resultSet1.getString("station_name");
                temp.stationCode = resultSet1.getString("station_code");
                temp.stationDetailId = resultSet1.getInt("station_detail_id");
                temp.lineId = resultSet1.getInt("line_id");
                temp.beforeStation = resultSet1.getInt("before_station");
                temp.nextStation = resultSet1.getInt("next_station");
                temp.stationId = resultSet1.getInt("station_id");
                station.add(temp);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }

    /*public static subwayData getStationDataWithIdDB(int stationDetailId)
    * station detail id로 해당 역과 관련된 정보를 가져옴
    * */
    public static subwayData getStationDataWithIdDB(int stationDetailId) {
        subwayData station = new subwayData();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery1;
            strQuery1 = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, " +
                    "next_station, " + "station_id FROM Subway.sub_line_name_info WHERE station_detail_id = %d", stationDetailId);
            java.sql.ResultSet resultSet1 = stmt.executeQuery(strQuery1);
            while(resultSet1.next()) {
                station.stationName = resultSet1.getString("station_name");
                station.stationCode = resultSet1.getString("station_code");
                station.stationDetailId = resultSet1.getInt("station_detail_id");
                station.lineId = resultSet1.getInt("line_id");
                station.beforeStation = resultSet1.getInt("before_station");
                station.nextStation = resultSet1.getInt("next_station");
                station.stationId = resultSet1.getInt("station_id");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }

    /*public static ArrayList<timetable> getScheduleDataDB(subwayData parent, subwayData child)
    * child는 parent의 다음 역
    * parent를 출발 시각 이후의 열차가 child에 몇 시에 있는지 확인*/
    public static ArrayList<timeTable> getScheduleDataDB(subwayData parent, subwayData child) {
        ArrayList<timeTable> schedules = new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, line_direction, subway_type, week_type, schedule_name, hour, minute, line_id " +
                            "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND hour - %d <= 1 AND ((hour * 60 + minute) " +
                            "- (%d * 60 + %d)) >= 0 AND week_type = \'%s\' AND line_direction = %d LIMIT 3 ",
                    child.lineId, child.stationDetailId, parent.schedule.hour, parent.schedule.hour, parent.schedule.minute,
                    parent.schedule.weekType, child.schedule.lineDirection, parent.schedule.typeName);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            System.out.println(strQuery);
            while(resultSet.next()) {
                timeTable temp = new timeTable();
                temp.hour = resultSet.getInt("hour");
                temp.minute = resultSet.getInt("minute");
                temp.lineDirection = resultSet.getInt("line_direction");
                temp.weekType = resultSet.getString("week_type");
                temp.typeName = resultSet.getString("subway_type");
                temp.scheduleName = resultSet.getString("schedule_name");
                schedules.add(temp);
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return schedules;
    }

    /*public staic timeTable getOneScheduleDataDB(subwayData parent, subwayData child, int i)
    * */
    public static timeTable getOneScheduleDataDB(subwayData parent, subwayData child, int i) {
        timeTable schedule = new timeTable();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            if(child.stationDetailId == 370) {
                System.out.println("HELLO");
            }
            strQuery = String.format("SELECT station_detail_id, line_direction, subway_type, week_type, schedule_name, hour, " +
                            "minute, line_id " + "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND hour - %d <= 1 AND " +
                            "((hour * 60 + minute) - (%d * 60 + %d)) >= 0 AND week_type = \'%s\' AND line_direction = %d " +
                            "AND subway_type = \'%s\' AND schedule_name = \'%s\' LIMIT 1 ",
                    parent.lineId, child.stationDetailId, parent.candiSchedule[i].hour, parent.candiSchedule[i].hour,
                    parent.candiSchedule[i].minute, parent.schedule.weekType, parent.candiSchedule[i].lineDirection,
                    parent.candiSchedule[i].typeName, parent.candiSchedule[i].scheduleName);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            System.out.println(strQuery);
            while(resultSet.next()) {
                schedule.hour = resultSet.getInt("hour");
                schedule.minute = resultSet.getInt("minute");
                schedule.lineDirection = resultSet.getInt("line_direction");
                schedule.weekType = resultSet.getString("week_type");
                schedule.typeName = resultSet.getString("subway_type");
                schedule.scheduleName = resultSet.getString("schedule_name");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return schedule;
    }

    public static timeTable getEndScheduleDataDB(subwayData parent, int stationDetailId , int lineDirection, int i) {
        timeTable schedule = new timeTable();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, line_direction, subway_type, week_type, schedule_name, hour, " +
                            "minute, line_id " + "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND week_type = \'%s\' " +
                            "AND line_direction = %d AND subway_type = \'%s\' LIMIT 1 ",
                    parent.lineId, stationDetailId,  parent.schedule.weekType, lineDirection, parent.candiSchedule[i].typeName);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            while(resultSet.next()) {
                schedule.hour = resultSet.getInt("hour");
                schedule.minute = resultSet.getInt("minute");
                schedule.lineDirection = resultSet.getInt("line_direction");
                schedule.weekType = resultSet.getString("week_type");
                schedule.typeName = resultSet.getString("subway_type");
                schedule.scheduleName = resultSet.getString("schedule_name");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return schedule;
    }

    public static void getTransferDataDB(int parentStationDetailId, int childStationDetailId, transfer transferInfo) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT start_station_detail_id, finish_station_detail_id, time_sec, distance " +
                    "FROM Subway.sub_transfer WHERE start_station_detail_id = %d AND finish_station_detail_id = %d",
                    parentStationDetailId, childStationDetailId);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            while(resultSet.next()) {
                transferInfo.startDetailId = resultSet.getInt("start_station_detail_id");
                transferInfo.finishDetailId = resultSet.getInt("finish_station_detail_id");
                transferInfo.distance = resultSet.getInt("distance");
                transferInfo.timeSec = resultSet.getInt("time_sec");
            }
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
    }
}