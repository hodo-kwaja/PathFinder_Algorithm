import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.sql.*;

class subwayData {
    String stationName; //역 이름
    String stationCode; //역 코드
    int stationDetailId;    //station_detail_id
    int lineId; //호선
    int typeName;   //급행역 여부
    boolean transfer;   //환승역 여부
    int direction;  //진행 방향
    int beforeStation;  //이전 역
    int nextStation;    //다음 역
    int duration;   //해댱역 소요시간
    int transferNum;    //해당역 환승 횟수
    int numStep;    //경유 정류장 수
    String scheduleName;    //종점
    String departureTime;   //출발 시간
    String arrivalTime; //도착 시간
    String congestion;  //혼잡도

    void getScheduleData(int hour, int minute, String weekType) {
        databaseManager dbManager = new databaseManager();
        dbManager.getScheduleData(stationDetailId, weekType, hour, minute);
    }
}

class Tree {
    class Node {
        subwayData data = new subwayData(); //데이터
        ArrayList<Node> child = new ArrayList<Node>();    //자식 노드
    }



    Node makeNode(subwayData nodeData) {
        Node node = new Node();
        node.data = nodeData;
        return node;
    }
    void addChild(Node parentNode, Node childNode) {
        parentNode.child.add(childNode);
    }


}

class databaseManager {

     Connection connectDatabase() {
        String jdbcDriver = "com.mysql.jdbc.Driver";
        String dbURL = "jdbc:mysql://localhost:3306/?user=root";
        String userName = "root";
        String password = "19980316";
        Connection conn = null;
        try {
            Class.forName(jdbcDriver);
            conn = DriverManager.getConnection(dbURL, userName, password);
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return conn;
    }

    ArrayList getStationData(String stationName) {
         Connection conn = connectDatabase();
         ArrayList<subwayData> station = new ArrayList<subwayData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE station_name = \"%s\"", stationName);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            while(resultSet.next()) {
                subwayData temp = new subwayData();
                temp.stationName = resultSet.getString("station_name");
                temp.stationCode = resultSet.getString("station_code");
                temp.stationDetailId = resultSet.getInt("station_detail_id");
                temp.lineId = resultSet.getInt("line_id");
                temp.beforeStation = resultSet.getInt("before_station");
                temp.nextStation = resultSet.getInt("next_station");
                station.add(temp);
            }
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }
    void getTransferData() {

    }

    void getConnectStationData() {

    }

    void getScheduleData(int stationDetailId, String weekType, int hour, int minute) {
        Connection conn = connectDatabase();

    }
}

class timeAndDate {
    int hour;
    int minute;
    char weekType;
    LocalTime nowTime = LocalTime.now();
    LocalDate nowDate = LocalDate.now();
    void getHour() {
        hour  = nowTime.getHour();
    }
    void getMinute() {
        minute = nowTime.getMinute();
    }
    void getDayOfWeek() {
        //나중에 공휴일 처리하자
        DayOfWeek dayOfWeek = nowDate.getDayOfWeek();
        int dayOfWeekNumber = dayOfWeek.getValue();
        System.out.println(dayOfWeekNumber);
        if (dayOfWeekNumber == 7) {
            weekType = 'U';
        }
        else if (dayOfWeekNumber == 6) {
            weekType = 'A';
        }
        else {
            weekType = 'W';
        }
    }

}
public class Metro_Navi {
    public static void main(String[] args) {
        databaseManager dbManager = new databaseManager();
        ArrayList<subwayData> subData = new ArrayList<subwayData>();
        Tree tree = new Tree();

        System.out.print("출발역, 도착역, 시, 분, 요일 : ");
        Scanner input = new Scanner(System.in);

        String departureStation = input.next();
        String destinationStation = input.next();
        int hour = Integer.parseInt(input.next());
        int minute = Integer.parseInt(input.next());
        String weekType = input.next();
        System.out.println(departureStation + ' ' + destinationStation + ' ' + hour + ' ' + minute + ' ' + weekType);

        dbManager.getStationData(departureStation);
        dbManager.getStationData(destinationStation);
    }
}

