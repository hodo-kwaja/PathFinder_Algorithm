import java.util.*;
import java.sql.*;

class subwayData {
    String stationName; //역 이름
    String stationCode; //역 코드
    int stationDetailId;    //station_detail_id
    int lineId; //호선
    int type;   //급행역 여부
    boolean transfer;   //환승역 여부
    String departureTime;   //출발 시간
    String arrivalTime; //도착 시간
    String congestion;  //혼잡도
    int congestionValue;    //혼잡도 값
}

class node {
    subwayData data = new subwayData(); //데이터
    ArrayList<node> sibling = new ArrayList<node>();    //자식 노드
}

class tree {

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

    subwayData getStationData(String departureStation) {
         Connection conn = connectDatabase();
         java.sql.ResultSet rs = null;
         subwayData station = new subwayData();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE station_name = \"%s\"", departureStation);
            System.out.print(strQuery);
            rs = stmt.executeQuery(strQuery);
            while(rs.next()) {
                station.stationName = rs.getString("station_name");
                station.stationCode = rs.getString("station_code");
                station.stationDetailId = rs.getInt("station_detail_id");
                station.lineId = rs.getInt("line_id");
                station.type = rs.getInt("station_name");
                station.stationName = rs.getString("station_name");

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
}

class user {

}

public class Metro_Navi {
    public static void main(String[] args) {
        databaseManager dbManager = new databaseManager();
        subwayData subData = new subwayData();
        System.out.print("출발 역 입력 : ");
        Scanner input = new Scanner(System.in);
        String departureStation = input.next();
        subData = dbManager.getStationData(departureStation);
        System.out.print(subData.stationDetailId);
        }
}

