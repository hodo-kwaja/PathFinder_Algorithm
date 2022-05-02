import java.time.*;
import java.util.*;
import java.sql.*;

class subwayData {

    timeTable schedule = new timeTable();
    path path = new path();
    String stationName; //역 이름
    String stationCode; //역 코드
    int stationDetailId;    //station_detail_id
    int lineId; //호선
    boolean transfer;   //환승역 여부

    int beforeStation;  //이전 역
    int nextStation;    //다음 역

    String departureTime;   //출발 시간
    String arrivalTime; //도착 시간
    String congestion;  //혼잡도
    
}
class path {
    int duration;
    int transferNum;
    int numStep;
}

class timeTable {
    int stationDetailId;
    int lineDirection;
    int hour;
    int minute;
    String weekType;
    String scheduleName;
    String typeName;
}

/*노드 클래스*/
class Node {
    subwayData data = new subwayData(); //데이터
    Node beforeNode;   //부모 노드
    ArrayList<Node> child = new ArrayList<Node>();    //자식 노드
    Stack<subwayData> step = new Stack<subwayData>();   //중간 정류장
}

/*트리 클래스*/
class Tree {
    Node root = new Node(); //트리의 root 노드
    //Node parent;
    //Node grandParent;
    String departureStaionName; //출발역 이름
    String destinationStationName;  //도착역 이름
    int startHour, startMinute;
    String weekType;
    String[] shortestTime = new String[1091];   //역까지 최단 시간
    ArrayList<Node> path = new ArrayList<>();   //도착역에 도착한 leafNode들
    databaseManager dbManager = new databaseManager();

     /*void initRoot()
     * root노드 정보 업데이트*/
    void initRoot() {
        root.data.schedule.hour = startHour; //root 노드의 시간을 출발시각으로 업데이트
        root.data.schedule.minute = startMinute; //출발 시각으로 업데이트
        root.data.path.duration = 0; //
        root.data.path.transferNum = 0;
        root.data.path.numStep = 0;
        root.data.schedule.weekType = weekType;
    }

    int compareShortestTime() {
        int reult = 0;

        return reult;
    }

    /*void updateSchedule(Node parent, Node child)
    * child의 운행시간표 중 parent의 출발시각 이후의 시간표를 가져옴
    * if(현재 시각 이후 시간표 있으면)
    *   시간표 입력 AND return 0;
    * else
    *   return 1*/
    int updateSchedule(subwayData parent, subwayData child) {
        ArrayList<timeTable> schedules;
        schedules = dbManager.getScheduleDataDB(parent, child);
        try {
            child.schedule = schedules.get(0);
            return 0;
        }
        catch (IndexOutOfBoundsException e){
            return 1;
        }
    }

    void makeTree(Node parent, String stationName) {
        ArrayList<subwayData> possiblePath = getStationData(stationName);
        int i = 0;
        int count;
        while (i < possiblePath.size()) {
            parent.child.add(makeNode(possiblePath.get(i)));
            count = updateSchedule(parent.data, parent.child.get(i).data);
            if(count == 1) {
                parent.child.remove(i);
                possiblePath.remove(i);
            }
            else {
                i++;
            }
        }
        int j = 0;
        while(j < parent.child.size()) {

        }
    }

    /*void makeNode(subwayData newData)
    * 새로운 노드 생성
    * 매개변수로 받은 데이터를 새 노드에 복사
    * 생성된 노드 반환
    *
    * 입력
    * - subwayData newData : 노드에 입력하려는 데이터
    * 출력
    * - Node newStation : newData의 값이 들어간 새로운 노드*/
    Node makeNode(subwayData newData) {
        Node newStation = new Node();
        newStation.data = newData;
        return newStation;
    }

    /*makePath(subwayData newData)
    * 새로운 노드 생성
    * 매개변수로 입력 받은 데이터를 새 노드에 복사
    * 다음 환승역까지 중간 경로 탐색
    * 환승역을 발견하면 현재 노드의 자식으로 추가하고 종료
    *
    * 입력
    * - newData : 노드에 입력하려는 데이터*/
    void makePath(subwayData newData) {
        Node newStation = new Node();   //새로운 노드 할당
        newStation.data = newData;  //노드에 데이터 복사
        if (newStation.data.schedule.lineDirection == 1) {   //lineDirection = 1 -> 상행선
            searchUpStep(newStation, newStation.data.beforeStation);  //beforeStaion으로 검색
        }
        else {  //lineDirection = 0 -> 하행선
            searchDownStep(newStation, newStation.data.nextStation);    //nextStaion으로 검색
        }
    }

    /*ArrayList getStationData(String stationName)
    * 역 이름으로 해당 역에서 갈 수 있는 역 탐색
    *
    * 입력
    * - stationName : 검색하려는 역 이름
    * 출력
    * - searchPossiblePath()를 통해 반환된 상,하행으로 분류된 경로*/
    ArrayList getStationData(String stationName) {
           ArrayList<subwayData> station = new ArrayList<>();
           station = dbManager.getStationDataDB(stationName); //역 이름으로 역 검색
           return searchPossiblePath(station);
    }

    /*ArrayList getStationData(int stationDetailId)
    * station_detail_id로 역 정보 가져오기
    *
    * 입력
    * - stationDetailId : 검색하려는 역 station_detail_id
    * 출력
    * - subwayData station : 검색한 역의 데이터*/
    subwayData getStationData(int stationDetailId) {
        subwayData station = new subwayData();
        station = dbManager.getStationDataDB(stationDetailId);    //station_detail_id로 역 검색
        return station;
    }

    /*ArrayList searchPossiblePath(ArrayList<subwayData> station)
    * 역에서 상, 하행으로 경로 나눔
    *
    * 입력
    * - ArrayList<subwayData> station : 연결된 역들
    * 출력
    * - ArrayList<subwayData> possiblePath : 상, 하행으로 분류된 경로*/
    ArrayList searchPossiblePath(ArrayList<subwayData> station) {
        ArrayList<subwayData> possiblePath = new ArrayList<>();
        subwayData temp = new subwayData();
        int i = 0;
        while(i < station.size()) { //station의 수만큼
            temp = station.get(i);
            if(temp.nextStation != 0) { //nextStation의 값이 있으면 -> 하행선 경로가 있다
                subwayData downStation = new subwayData();
                downStation.stationName = temp.stationName;
                downStation.stationCode = temp.stationCode;
                downStation.stationDetailId = temp.stationDetailId;
                downStation.lineId = temp.lineId;
                downStation.beforeStation = temp.beforeStation;
                downStation.nextStation = temp.nextStation;
                downStation.schedule.lineDirection = 1;
                possiblePath.add(downStation);  //possiblePath에 경로 추가
            }
             if(temp.beforeStation != 0) {  //beforeStaion의 값이 있으면 -> 상행선 경로가 있다
                 subwayData upStation = new subwayData();
                 upStation.stationName = temp.stationName;
                 upStation.stationCode = temp.stationCode;
                 upStation.stationDetailId = temp.stationDetailId;
                 upStation.lineId = temp.lineId;
                 upStation.beforeStation = temp.beforeStation;
                 upStation.nextStation = temp.nextStation;
                 upStation.schedule.lineDirection = 0;
                 possiblePath.add(upStation);   //possiblePath에 경로 추가
            }
             i++;
        }
        return possiblePath;
    }

    /*Node searchUpStep(int stationDetailId)
    * beforeStation로 상행인 다음 경로들 탐색
    * 만약 환승역을 찾으면 -> 환승역을 현재 노드의 자식으로 추가 -> 반복문 탈출
    *
    * 입력
    * Node parent : 부모 노드
    * - int stationDetailId :
    * 출력
    * Stack <subwayData> stepPath : 중간 경로를 담은 스택*/
    Stack<subwayData> searchUpStep(Node parent, int stationDetailId) {
        Stack<subwayData> stepPath = new Stack<>();
        subwayData temp = new subwayData();
        int nextStationDetailId = stationDetailId;
        while(true) {
            temp = getStationData(nextStationDetailId);
            if(checkTransfer(temp.stationCode)) {
                addChild(parent, makeNode(temp));
                break;
            }
            else {
                stepPath.push(temp);
                nextStationDetailId = temp.stationDetailId;
            }
        }
        return stepPath;
    }

    /*Node searchDownStep(int stationDetailId)
     * nextStation으로 하행인 다음 경로를 탐색 */
    Stack<subwayData> searchDownStep(Node parent, int stationDetailId) {
        Stack<subwayData> stepPath = new Stack<>();
        subwayData temp = new subwayData();
        int nextStationDetailId = stationDetailId;
        while(true) {
            temp = getStationData(nextStationDetailId);
            if(checkTransfer(temp.stationCode)) {
                addChild(parent, makeNode(temp));
                break;
            }
            else {
                stepPath.push(temp);
                nextStationDetailId = temp.stationDetailId;
            }
        }
        return stepPath;
    }

    /*void addChild(Node parent, Node child)
    * parent 노드에 child 노드를 연결
    *
    * 입력
    * - Node parent : 부모 노드
    * - Node child : 자식 노드*/
    void addChild(Node parent, Node child) {
        parent.child.add(child);
        child.beforeNode = parent;
    }

    /*int checkTransfer(int stationCode)
    * stationCode로 검색해서 결과가 2개 이상이면 환승역으로 판단
    *
    * 입력
    * - int stationCode : 역 코드
    * 출력
    * - boolean result : 1 -> 환승역, 0 -> 환승역 아님*/
    boolean checkTransfer(String stationCode) {
        boolean result = true;

        return result;
    }
}

class databaseManager {

    timeAndDate time = new timeAndDate();

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

    ArrayList getStationDataDB(String stationName) {
         Connection conn = connectDatabase();
         ArrayList<subwayData> station = new ArrayList<subwayData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery1, strQuery2;
            strQuery1 = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE station_name = \"%s\"", stationName);
            java.sql.ResultSet resultSet1 = stmt.executeQuery(strQuery1);
            while(resultSet1.next()) {
                subwayData temp = new subwayData();
                temp.stationName = resultSet1.getString("station_name");
                temp.stationCode = resultSet1.getString("station_code");
                temp.stationDetailId = resultSet1.getInt("station_detail_id");
                temp.lineId = resultSet1.getInt("line_id");
                temp.beforeStation = resultSet1.getInt("before_station");
                temp.nextStation = resultSet1.getInt("next_station");
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

    subwayData getStationDataDB(int stationDetailId) {
        Connection conn = connectDatabase();
        subwayData station = new subwayData();

        return station;
    }

    void getTransferDataDB() {

    }

    subwayData getUpConnectStationDataDB(int stationDetailId) {
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

    subwayData getDownConnectStationDataDB(int stationDetailId) {
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

    ArrayList<timeTable> getScheduleDataDB(subwayData parent, subwayData child) {
        Connection conn = connectDatabase();
        ArrayList<timeTable> schedules = new ArrayList<>();
        //time.calculateTime(parent.schedule.hour, parent.schedule.minute, child);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, line_direction, subway_type, week_type, schedule_name, hour, minute, line_id " +
                    "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND hour >= %d AND minute >= %d AND week_type = \'%s\' AND line_direction = %d LIMIT 5",
                    child.lineId, child.stationDetailId, parent.schedule.hour, parent.schedule.minute, parent.schedule.weekType, parent.schedule.lineDirection);
            System.out.println(strQuery);
            java.sql.ResultSet resultSet = stmt.executeQuery(strQuery);
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

    void calculateTime(int hour, int minute, subwayData child) {
        child.schedule.hour = hour;
        child.schedule.minute = minute;
        child.schedule.minute = minute + 15;
        if(child.schedule.minute >= 60) {
            child.schedule.minute = child.schedule.minute - 60;
            child.schedule.hour = child.schedule.hour + 1;
        }
    }
}

public class Metro_Navi {
    public static void main(String[] args) {
        databaseManager dbManager = new databaseManager();
        ArrayList<subwayData> subData = new ArrayList<subwayData>();
        String departureStaionName;
        String destinationStationName;

        timeAndDate time = new timeAndDate();
        Tree tree = new Tree();



        System.out.print("출발역, 도착역, 시, 분, 요일 : ");
        Scanner input = new Scanner(System.in);

        tree.departureStaionName = input.next();
        tree.destinationStationName = input.next();
        tree.startHour = Integer.parseInt(input.next());
        tree.startMinute = Integer.parseInt(input.next());
        tree.weekType = input.next();

        tree.initRoot();
        tree.makeTree(tree.root, tree.departureStaionName);
        System.out.println(tree.departureStaionName);

/*        //subData = dbManager.getStationData(departureStaionName);
        subData = tree.getStationData(departureStaionName);*/
    }
}

