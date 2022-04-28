import java.time.*;
import java.util.*;
import java.sql.*;

class subwayData {
    String stationName; //역 이름
    String stationCode; //역 코드
    int stationDetailId;    //station_detail_id
    int lineId; //호선
    int typeName;   //급행역 여부
    boolean transfer;   //환승역 여부
    int lineDirection = -1;  //진행 방향
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

/*트리 클래스*/
class Tree {
    //Node root = new Node(); //트리의 root 노드
    //Node parent;
    //Node grandParent;
    //String departureStaionName; //출발역 이름
    //String destinationStationName;  //도착역 이름
    //int hour, minute;
    //String weekType;
    //String[] shortestTime = new String[1091];   //역까지 최단 시간
    //ArrayList<Node> path = new ArrayList<>();   //도착역에 도착한 leafNode들
    databaseManager dbManager = new databaseManager();

    /*노드 클래스*/
    class Node {
        subwayData data = new subwayData(); //데이터
        Node beforeNode = new Node();   //부모 노드
        ArrayList<Node> child = new ArrayList<Node>();    //자식 노드
        Stack<subwayData> step = new Stack<subwayData>();   //중간 정류장
    }

    void makeTree(Node parent, String stationName) {
        ArrayList<subwayData> possiblePath = getStationData(stationName);
        int i = 0;
        while (i < possiblePath.size()) {
            MakeNode(possiblePath.get(i));
        }
    }

    void MakeNode(subwayData newData) {
        Node newStation = new Node();
        newStation.data = newData;
        if (newStation.data.lineDirection == 1) {
            searchStep(newStation.data.beforeStation);
        }
        else {
            searchStep(newStation.data.nextStation);
        }
    }

    ArrayList getStationData(String stationName) {
           ArrayList<subwayData> station = new ArrayList<>();
           station = dbManager.getStationData(stationName);
           return searchPossiblePath(station);
    }
    ArrayList getStationData(int stationDetailId) {
        
    }
    ArrayList searchPossiblePath(ArrayList<subwayData> station) {
        ArrayList<subwayData> poosiblePath = new ArrayList<>();
        subwayData temp = new subwayData();
        int i = 0;
        while(i < station.size()) {
            temp = station.get(i);
            if(temp.nextStation != 0) {
                subwayData downStation = new subwayData();
                downStation.stationName = temp.stationName;
                downStation.stationCode = temp.stationCode;
                downStation.stationDetailId = temp.stationDetailId;
                downStation.lineId = temp.lineId;
                downStation.beforeStation = temp.beforeStation;
                downStation.nextStation = temp.nextStation;
                downStation.lineDirection = 1;
                poosiblePath.add(downStation);
            }
             if(temp.beforeStation != 0) {
                 subwayData upStation = new subwayData();
                 upStation.stationName = temp.stationName;
                 upStation.stationCode = temp.stationCode;
                 upStation.stationDetailId = temp.stationDetailId;
                 upStation.lineId = temp.lineId;
                 upStation.beforeStation = temp.beforeStation;
                 upStation.nextStation = temp.nextStation;
                 upStation.lineDirection = 0;
                 poosiblePath.add(upStation);
            }
             i++;
        }
        return poosiblePath;
    }

    subwayData searchStep(int stationDetailId) {

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
    subwayData getStationData(int stationDetailId) {
        Connection conn = connectDatabase();
        subwayData station = new subwayData();

        return station;
    }

    void getTransferData() {

    }

    subwayData getUpConnectStationData(int stationDetailId) {
        Connection conn = connectDatabase();
        subwayData station = new subwayData();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE before_station = %d", stationDetailId);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            while(resultSet.next()) {
                station.stationName = resultSet.getString("station_name");
                station.stationCode = resultSet.getString("station_code");
                station.stationDetailId = resultSet.getInt("station_detail_id");
                station.lineId = resultSet.getInt("line_id");
                station.beforeStation = resultSet.getInt("before_station");
                station.nextStation = resultSet.getInt("next_station");
            }
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }

    subwayData getDownConnectStationData(int stationDetailId) {
        Connection conn = connectDatabase();
        subwayData station = new subwayData();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE next_station = %d", stationDetailId);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
            while(resultSet.next()) {
                station.stationName = resultSet.getString("station_name");
                station.stationCode = resultSet.getString("station_code");
                station.stationDetailId = resultSet.getInt("station_detail_id");
                station.lineId = resultSet.getInt("line_id");
                station.beforeStation = resultSet.getInt("before_station");
                station.nextStation = resultSet.getInt("next_station");
            }
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
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
        String departureStaionName;
        String destinationStationName;
        Tree tree = new Tree();

        System.out.print("출발역, 도착역, 시, 분, 요일 : ");
        Scanner input = new Scanner(System.in);

        departureStaionName = input.next();
        destinationStationName = input.next();
        //tree.hour = Integer.parseInt(input.next());
        //tree.minute = Integer.parseInt(input.next());
        //tree.weekType = input.next();

        //subData = dbManager.getStationData(departureStaionName);
        subData = tree.getStationData(departureStaionName);
    }
}

