import java.time.*;
import java.util.*;
import java.sql.*;

/*역 정보*/
class subwayData {

    timeTable schedule = new timeTable();
    timeTable[] candiSchedule = new timeTable[3];
    String stationName; //역 이름
    String stationCode; //역 코드
    int stationDetailId;    //station_detail_id
    int lineId; //호선
    boolean transfer;   //환승역 여부
    int stationId;
    int beforeStation;  //이전 역
    int nextStation;    //다음 역

    String departureTime;   //출발 시간
    String arrivalTime; //도착 시간
    String congestion;  //혼잡도

}

/*열차 시간표*/
class timeTable {
    int lineDirection;  //진행 방향
    int hour = 99;   //출발 시각(시)
    int minute = 99; //출발 시각(분)
    String weekType;    //요일
    String scheduleName;    //종착 지점
    String typeName;    //열차 종류
    int duration;   //소요 시간
    int transferNum;    //환승 횟수
    int numStep;    //정류장 수
}

/*노드*/
class Node {
    boolean isAlive = true;    //살아있는지 판단
    subwayData data = new subwayData(); //데이터
    Node beforeNode;   //부모 노드
    ArrayList<Node> child = new ArrayList<Node>();    //자식 노드
    Stack<subwayData> step = new Stack<subwayData>();   //중간 정류장
}

/*트리*/
class Tree {
    Node root = new Node(); //트리의 root 노드
    String departureStaionName; //출발역 이름
    String destinationStationName;  //도착역 이름
    int startHour, startMinute; //출발 시각(시, 분)
    String weekType;    //요일
    Node[] shortestNode = new Node[644];   //역까지 최단 시간으로 도착하는 노드들
    ArrayList<Node> path = new ArrayList<>();   //도착역에 도착한 노드들
    Queue<Node> queue = new LinkedList<>(); //넓이우선탐색 할때 쓸 큐

    databaseManager dbManager = new databaseManager();

     /*void initRoot()
     * root노드 정보 업데이트*/
    void initRoot() {
        root.data.stationName = departureStaionName;    //출발역
        root.data.schedule.hour = startHour; //출발 시각
        root.data.schedule.minute = startMinute;
        root.data.schedule.duration = 0; //소요 시간
        root.data.schedule.transferNum = 0; //환승 수
        root.data.schedule.numStep = 0; //정거장 수
        root.data.schedule.weekType = weekType; //요일
        queue.add(root);    //queue에 root노드 넣음
    }

    /*int convertTime(int hour, int minute)
    * 역 간 소요 시간을 계산하기 위해서 시간 변환을 위해 사용
    *
    * 입력
    * - int hour : 시
    * - int minute : 분
    *
    * 출력
    * - int num : 변환된 시간*/
    int convertTime (int hour, int minute) {
        int num = hour * 60 + minute;
        return num;
    }

    /*void updatePathInfo (subwayData parent, subwayData child)
    * child의 PathInfo를 업데이트함
    *
    * 입력
    * - subwayData parent : 부모 정보
    * - subwayData child : 자식 정보*/
    void updatePathInfo(subwayData parent, subwayData child) {
        for (int i = 0; i < 3; i++) {
            if (parent.candiSchedule[i] != null) {
                int beforeTime = convertTime(parent.candiSchedule[i].hour, parent.candiSchedule[i].minute); //부모 변환 시간
                int afterTime = convertTime(child.candiSchedule[i].hour, child.candiSchedule[i].minute);    //자식 변환 시간
                child.candiSchedule[i].duration = afterTime - beforeTime;   //소요 시간
                child.candiSchedule[i].numStep = parent.candiSchedule[i].numStep + 1;   //정거장 수
                child.candiSchedule[i].transferNum = parent.candiSchedule[i].transferNum;   //환승 수
            }
        }
    }

    /*boolean compareShortestTime(subwayData data)
    * shortestNode 배열에 저장된 현재 XX역 까지의 최소 소요 시간 노드랑 새로운 XX역 까지의 최소 소요 시간이랑 비교
    * 새로운 경로가 기존 경로보다 빠르면 shortestNode의 값을 변경함
    * 기존 경로 노드의 isAlive 값을 false로 만들어서 그 경로는 더이상 탐색하지 않도록 함
    *
    * 입력
    * - subwayData data : 새로운 경로의 데이터
    *
    * 출력
    * - boolean result : true -> 갱신, false -> 유지*/
    boolean compareShortestTime(subwayData data) {
        boolean result = true;
        try {
            int currentTime = convertTime(shortestNode[data.stationId].data.schedule.hour, shortestNode[data.stationId].data.schedule.minute);  //기존 시간
            int newTime = convertTime(data.schedule.hour, data.schedule.minute);    //새로운 시간

            if (currentTime > newTime) {    //기존 시간 > 새로운 시간
                shortestNode[data.stationId].isAlive = false;   //기존 노드 isAlive값 false로 변경
                return result;  //result = true
            }
            else {  //기존 시간 < 새로운 시간
                result = false;
                return result;  //result = false
            }
        } catch (NullPointerException e) {  //기존 시간이 없으면
            return result;  //result = true
        }
    }

    /*int updateSchedule(Node parent, Node child)
    * child의 운행시간표 중 parent의 출발시각 이후의 시간표를 가져옴
    *
    * 추후 업데이트해야 할 사항
    * 1. scheduleName 보고 목적지까지 가는지 탐색
    * 2. 급행 열차 시간표 있는지 탐색
    *
    * 입력
    * - subwayData parent : 부모 데이터
    * - subwayData child : 자식 데이터
    *
    * 출력
    * - true -> 시간표 있음, false -> 시간표 없음*/
    ArrayList getScheduleData(subwayData parent, subwayData child) {
        ArrayList<timeTable> schedules;
        schedules = dbManager.getScheduleDataDB(parent, child); //parent 이후의 시간에서 child의 시간표를 3개 가져옴
        try {
            return refineSchedule(schedules);
        }
        catch (IndexOutOfBoundsException e){    //만약 이후 시간표가 없으면 ex)막차 끊김
            System.out.println("열차 없음");
            return schedules;
        }
    }

    ArrayList refineSchedule (ArrayList<timeTable> schedules) {
        ArrayList<timeTable> newSchedule = new ArrayList<>();
        newSchedule.add(schedules.get(0));
        try {
            if (schedules.get(0).scheduleName.equals(schedules.get(1).scheduleName)) {
                if (!schedules.get(0).typeName.equals(schedules.get(1).typeName)) {
                    newSchedule.add(schedules.get(1));
                }
            }
            else {
                newSchedule.add(schedules.get(1));
            }
            if (schedules.get(0).scheduleName.equals(schedules.get(2).scheduleName)) {
                if (!schedules.get(0).typeName.equals(schedules.get(2).typeName)) {
                    if (schedules.get(1).scheduleName.equals(schedules.get(2).scheduleName)) {
                        if (!schedules.get(1).typeName.equals(schedules.get(2).typeName)) {
                            newSchedule.add(schedules.get(2));
                        }
                    }
                }
            }
            else {
                newSchedule.add(schedules.get(2));
            }
        } catch (IndexOutOfBoundsException e) {

        }

        return newSchedule;
    }

    /*void makeTree()
    * 제일 중요한 놈 얘가 다아아아 해먹음*/
    void makeTree() {
        while (queue.size() != 0) { //queue가 차있으면
            Node parent = queue.poll(); //parent에 하나 끌고 옴
            if(parent.isAlive) {    //끌고온 노드가 유효하면
                makeSubTree(parent);  //makeRoot에 넣어서 분기되는 경로 탐색
                int i = 0;
                while (i < parent.child.size()) {   //parent.child -> 분기되는 경로 수
                    Node child = makeRoute(parent.child.get(i));  //자식 노드에 경로 하나 끌고 옴
                    if (compareShortestTime(child.data)) {  //새로운 경로의 유효성 검증
                        shortestNode[child.data.stationId] = child;
                        queue.add(child);
                    }
                    i++;
                }
            }
        }
    }

    /*boolean deleteDuplication(subwayData parent, subwayData child)
    * makeSubtree()에서 자식노드로 부모랑 똑같은 것을 추가하는 것을 방지하기 위함
    *
    * 입력
    * - subwayData parent : 부모 데이터
    * - subwayData child : 자식 데이터
    *
    * 출력
    * - boolean result : true -> 중복임, false -> 중복 아님*/
    boolean deleteDuplicationPath(subwayData parent, subwayData child) {
        boolean result = false;
        if(parent.lineId == child.lineId) { //parent와 child의 호선이 같으면
            if(parent.schedule.lineDirection != child.schedule.lineDirection) { //자기가 전에 왔던 경로와 같은지 판단
                result = true;  //진행
            }
        }
        return result;
    }

    /*void updateBestTime(Node station)
    * 시간표 후보들 중 가장 빠른 것을 결정
    * */
    void updateBestTime(subwayData data, ArrayList<timeTable> schedules) {
        int i = 0;
        int index = 0;
        int candiTime = 99999;
        int temp;
        while(i <schedules.size()) {
            temp = convertTime(schedules.get(i).hour, schedules.get(i).minute);
            if( candiTime < temp) {
                candiTime = temp;
                index = i;
            }
            i++;
        }
        data.schedule = data.candiSchedule[index];
    }

    /*void makeSubTree(Node parent)
    * parent랑 연결된 노드가 몇 개인지 탐색
    * 분기가 몇 갈래로 되는지
    *
    * 입력
    * - Node parent : 부모 노드*/
    void makeSubTree(Node parent) {
        ArrayList<subwayData> possiblePath = getStationDataWithName(parent.data.stationName);   //역 이름으로 경로 탐색
        int i = 0;
        while (i < possiblePath.size()) {   //탐색된 경로의 수만큼
            Node temp = makeNode(possiblePath.get(i));
            if(!deleteDuplicationPath(parent.data, temp.data)) {
                //addChild(parent, child);
                ArrayList<timeTable> schedules = getScheduleData(parent.data, temp.data);
                if (schedules.size() > 0) {
                    Node child = makeNode(temp.data);
                    int j = 0;
                    while(j < schedules.size()) {
                        child.data.candiSchedule[j] = schedules.get(j);
                        j++;
                    }
                    updateBestTime(child.data, schedules);
                    addChild(parent,child);
                }
            }
            i++;
        }
    }

    /*void makeRoute(Node parent)
    * */
    Node makeRoute(Node parent) {
        if (parent.data.schedule.lineDirection == 1) {
            addPath(parent,searchUpStep(parent, parent.data.beforeStation));
        } else {
            addPath(parent,searchDownStep(parent, parent.data.nextStation));
        }
        return parent.child.get(0);
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
        newStation.data.stationName = newData.stationName;
        newStation.data.stationCode = newData.stationCode;
        newStation.data.stationDetailId = newData.stationDetailId;
        newStation.data.lineId = newData.lineId;
        newStation.data.beforeStation = newData.beforeStation;
        newStation.data.nextStation = newData.nextStation;
        newStation.data.schedule.lineDirection = newData.schedule.lineDirection;
        return newStation;
    }

    /*ArrayList getStationData(String stationName)
    * 역 이름으로 해당 역에서 갈 수 있는 역 탐색
    *
    * 입력
    * - stationName : 검색하려는 역 이름
    * 출력
    * - searchPossiblePath()를 통해 반환된 상,하행으로 분류된 경로*/
    ArrayList getStationDataWithName(String stationName) {
           ArrayList<subwayData> station = new ArrayList<>();
           station = dbManager.getStationDataWithNameDB(stationName); //역 이름으로 역 검색
           return searchPossiblePath(station);
    }

    /*ArrayList getStationData(int stationDetailId)
    * station_detail_id로 역 정보 가져오기
    *
    * 입력
    * - stationDetailId : 검색하려는 역 station_detail_id
    * 출력
    * - subwayData station : 검색한 역의 데이터*/
    subwayData getStationDataWithId(int stationDetailId) {
        subwayData station = new subwayData();
        station = dbManager.getStationDataWithIdDB(stationDetailId);    //station_detail_id로 역 검색
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
        //subwayData temp = new subwayData();
        int i = 0;
        while(i < station.size()) { //station의 수만큼
            subwayData temp = station.get(i);
            if(temp.nextStation != 0) { //nextStation의 값이 있으면 -> 하행선 경로가 있다
                subwayData downStation = new subwayData();
                downStation.stationName = temp.stationName;
                downStation.stationCode = temp.stationCode;
                downStation.stationDetailId = temp.stationDetailId;
                downStation.lineId = temp.lineId;
                downStation.beforeStation = temp.beforeStation;
                downStation.nextStation = temp.nextStation;
                downStation.schedule.lineDirection = 0;
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
                 upStation.schedule.lineDirection = 1;
                 possiblePath.add(upStation);   //possiblePath에 경로 추가
            }
             i++;
        }
        return possiblePath;
    }

    void getOneScheduleData(subwayData parent, subwayData child) {
        for(int i = 0; i < 3; i++) {
            timeTable schedule;
            if(parent.candiSchedule[i] != null) {
                schedule = dbManager.getOneScheduleDataDB(parent, child, i); //parent 이후의 시간에서 child의 시간표를 3개 가져옴
                try {
                    child.candiSchedule[i] = schedule;
                } catch (IndexOutOfBoundsException e) {    //만약 이후 시간표가 없으면 ex)막차 끊김
                    System.out.println("열차 없음");
                }
            }
        }
    }

    /*Node searchUpStep(int stationDetailId)
    * beforeStation로 상행인 다음 경로들 탐색
    * 만약 환승역을 찾으면 -> 환승역을 현재 노드의 자식으로 추가 -> 반복문 탈출
    *
    * 입력
    * Node parent : 부모 노드
    * - int stationDetailId : parent의 전 역의 station_detail_id
    * 출력
    * Stack <subwayData> stepPath : 중간 경로를 담은 스택*/
    Stack<subwayData> searchUpStep(Node parent, int stationDetailId) {
        Stack<subwayData> stepPath = new Stack<>();
        subwayData temp = new subwayData();
        int nextStationDetailId = stationDetailId;
        while(true) {
            temp = getStationDataWithId(nextStationDetailId);
            temp.schedule.lineDirection = 1;
            if(checkTransfer(temp.stationName)) {
                getOneScheduleData(parent.data, temp);
                updatePathInfo(parent.data, temp);
                addChild(parent, makeNode(temp));
                break;
            }
            else {
                getOneScheduleData(parent.data, temp);
                updatePathInfo(parent.data, temp);
                if(temp.stationName.equals(destinationStationName)) {
                    Node destination = makeNode(temp);
                    addChild(parent,destination);
                    destination.isAlive = false;
                    path.add(destination);
                    break;
                }
                else {
                    stepPath.push(temp);
                    if (temp.beforeStation == 0) {
                        Node end = makeNode(temp);
                        addChild(parent, makeNode(temp));
                        end.isAlive = false;
                        break;
                    } else {
                        nextStationDetailId = temp.beforeStation;
                    }
                }
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
            temp = getStationDataWithId(nextStationDetailId);
            temp.schedule.lineDirection = 0;
            if(checkTransfer(temp.stationName)) {
                getOneScheduleData(parent.data, temp);
                updatePathInfo(parent.data, temp);
                addChild(parent, makeNode(temp));
                break;
            }
            else {
                getOneScheduleData(parent.data, temp);
                updatePathInfo(parent.data, temp);
                if(temp.stationName.equals(destinationStationName)) {
                    Node destination = makeNode(temp);
                    addChild(parent,destination);
                    destination.isAlive = false;
                    path.add(destination);
                    break;
                }
                else {
                    stepPath.push(temp);
                    if (temp.nextStation == 0) {
                        Node end = makeNode(temp);
                        addChild(parent, makeNode(temp));
                        end.isAlive = false;
                        break;
                    } else {
                        nextStationDetailId = temp.nextStation;
                    }
                }
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
    * stationName으로 검색해서 결과가 2개 이상이면 환승역으로 판단
    *
    * 입력
    * - int stationName :  검색하려는 역 이름
    * 출력
    * - boolean result : true -> 환승역, false -> 환승역 아님*/
    boolean checkTransfer(String stationName) {
        boolean result = false;
        if(dbManager.getStationDataWithNameDB(stationName).size() > 1) {
            result = true;
        }
        return result;
    }

    /*void addPath(Node temp, Stack<subwayData> stepPath
    * 입력으로 받은 중간노드 queue를 node의 queue에 복사
    *
    * 입력
    * - Node temp : 경로를 복사할 노드
    * - Stack<subwayData> stepPath : 복사할 경로로*/
    void addPath(Node temp, Stack<subwayData> stepPath) {
        temp.step.addAll(stepPath);
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

    ArrayList getStationDataWithNameDB(String stationName) {
         Connection conn = connectDatabase();
         ArrayList<subwayData> station = new ArrayList<subwayData>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery1, strQuery2;
            strQuery1 = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE station_name = \"%s\" AND city_id = 1000", stationName);
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
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }

    subwayData getStationDataWithIdDB(int stationDetailId) {
        Connection conn = connectDatabase();
        subwayData station = new subwayData();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery1, strQuery2;
            strQuery1 = String.format("SELECT station_detail_id, station_name, station_code, line_id, before_station, next_station, " +
                    "station_id FROM Subway.sub_line_name_info WHERE station_detail_id = %d", stationDetailId);
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
            //System.out.println("DB 연결 성공");
        } catch (ClassNotFoundException e) {
            System.out.println("드라이버 로드 에러");
        } catch (SQLException e) {
            System.out.println("DB 연결 에러");
        }
        return station;
    }

    void getTransferDataDB() {

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
                    "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND hour - %d <= 1 AND ((hour * 60 + minute) - (%d * 60 + %d)) >= 0 AND week_type = \'%s\' AND line_direction = %d LIMIT 3 ",
                    child.lineId, child.stationDetailId, parent.schedule.hour, parent.schedule.hour, parent.schedule.minute, parent.schedule.weekType, child.schedule.lineDirection, parent.schedule.typeName);
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

    timeTable getOneScheduleDataDB(subwayData parent, subwayData child, int i) {
        Connection conn = connectDatabase();
        timeTable schedule = new timeTable();
        //time.calculateTime(parent.schedule.hour, parent.schedule.minute, child);
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Statement stmt = conn.createStatement();
            String strQuery;
            strQuery = String.format("SELECT station_detail_id, line_direction, subway_type, week_type, schedule_name, hour, minute, line_id " +
                            "FROM Subway.sub_tt_line_%d WHERE station_detail_id = %d AND hour - %d <= 1 AND ((hour * 60 + minute) - (%d * 60 + %d)) >= 0 AND week_type = \'%s\' AND line_direction = %d AND subway_type = \'%s\' LIMIT 1 ",
                    parent.lineId, child.stationDetailId, parent.candiSchedule[i].hour, parent.candiSchedule[i].hour, parent.candiSchedule[i].minute, parent.schedule.weekType, parent.candiSchedule[i].lineDirection, parent.candiSchedule[i].typeName);
            System.out.println(strQuery);
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


        int[] shortestTime = new int[1091];   //역까지 최단 시간
        Arrays.fill(shortestTime, 1);

        System.out.print("출발역, 도착역, 시, 분, 요일 : ");
        Scanner input = new Scanner(System.in);

        tree.departureStaionName = input.next();
        tree.destinationStationName = input.next();
        tree.startHour = Integer.parseInt(input.next());
        tree.startMinute = Integer.parseInt(input.next());
        tree.weekType = input.next();

        long beforeTime = System.currentTimeMillis();
        tree.initRoot();
        tree.makeTree();
        long afterTime = System.currentTimeMillis();
        long secDiffTime = (afterTime - beforeTime);
        System.out.println(secDiffTime);
        System.out.println(tree.departureStaionName);

/*        //subData = dbManager.getStationData(departureStaionName);
        subData = tree.getStationData(departureStaionName);*/
    }
}

