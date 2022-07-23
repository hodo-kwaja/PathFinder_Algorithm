package metro_navi;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

class Tree {
    Node root = new Node(); //트리의 root 노드
    String departureStaionName; //출발역 이름
    String destinationStationName;  //도착역 이름
    int startHour, startMinute; //출발 시각(시, 분)
    String weekType;    //요일
    Node[] shortestNode = new Node[644];   //역까지 최단 시간으로 도착하는 노드들
    ArrayList<Node> path = new ArrayList<>();   //도착역에 도착한 노드들
    Queue<Node> queue = new LinkedList<>(); //너비우선탐색 할때 쓸 큐
    databaseManager dbManager = new databaseManager();
    timeAndDate time = new timeAndDate();
    boolean finish = false;

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
     * 시, 분을 받아서 00:00분을 기준으로 몇 분이 흘렀는지로 변환
     *
     * 입력
     * - int hour : 시
     * - int minute : 분
     *
     * 출력
     * - int convertTime : 변환된 시간*/
    int convertTime (int hour, int minute) {
        int convertTime = hour * 60 + minute;
        return convertTime;
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
                child.candiSchedule[i].duration = afterTime - beforeTime;   //부모 to 자식 소요 시간
                child.candiSchedule[i].numStep = parent.candiSchedule[i].numStep + 1;   //정거장 수
                child.candiSchedule[i].transferNum = parent.candiSchedule[i].transferNum;   //환승 수
            }
        }
    }

    /*boolean compareShortestTime(subwayData data)
     * shortestNode 배열에 저장된 현재 XX역 까지의 최소 소요 시간 노드랑 새로운 XX역 까지의 최소 소요 시간이랑 비교
     * 새로운 경로가 더 빠르면 result = true
     * 기존 경로가 더 빠르면 result false
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
            int currentTime = convertTime(shortestNode[data.stationId].data.schedule.hour, shortestNode[data.stationId].data.schedule.minute);  //기존 최소 소요 시간
            int newTime = convertTime(data.schedule.hour, data.schedule.minute);    //새로운 소요 시간

            if (currentTime > newTime) {    //기존 시간 > 새로운 시간
                shortestNode[data.stationId].isAlive = false;   //기존 노드 isAlive값 false로 변경
                return result;  //result = true
            }
            else {  //기존 시간 <= 새로운 시간
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
            //System.out.println("열차 없음");
            return schedules;
        }
    }

    /*ArrayList refineSchedule (ArrayList<timeTable> schedules)
     * schedules[0], schedules[1], schedules[2] 중
     * 열차 타입, 종점이 중복되는 경로는 삭제함
     *
     * 입력
     * - ArrayList<timeTable> schedules : 중복 제거 전 경로
     *
     * 출력
     * - ArrayList<timeTable> newSchedule : 중복 제거 후 경로*/
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
        } catch (IndexOutOfBoundsException e) {}

        return newSchedule;
    }

    /*void makeTree()
     * 트리 만듦*/
    void makeTree() {
        while (queue.size() != 0) { //queue가 차있으면
            Node parent = queue.poll(); //parent에 하나 끌고 옴
            if (parent.isAlive) {    //끌고온 노드가 유효하면
                makeSubTree(parent);  //makeRoot에 넣어서 분기되는 경로 탐색
                int i = 0;
                while (i < parent.child.size()) {//parent.child -> 분기되는 경로 수
                    Node child = makeRoute(parent.child.get(i));  //자식 노드에 경로 하나 끌고 옴
                    if (compareShortestTime(child.data)) {  //새로운 경로의 유효성 검증
                        shortestNode[child.data.stationId] = child;
                        if(child.data.schedule.minute < 0) {
                            //System.out.println("hello");
                        }
                        queue.add(child);
                        if (finish) {
                            return;
                        }
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
            if(parent.schedule.lineDirection != child.schedule.lineDirection) { //진행 방향이 다를 경우
                if(parent.stationDetailId == child.stationDetailId) { //자기가 전에 왔던 경로와 같은지 판단
                    result = true;  //중복
                }
            }
        }
        return result;
    }

    /*void updateBestTime(Node station)
     * 시간표 후보들 중 가장 빠른 것을 선택
     *
     * 입력
     * - subwayData data : 정보 업데이트 하려는 역 데이터
     * - ArrayList<timeTable> schedules : 가능한 시간표들 */
    void updateBestTime(subwayData data, ArrayList<timeTable> schedules) {
        int i = 0;
        int index = 0;
        int candiTime = 99999;
        int temp;
        while(i <schedules.size()) {
            temp = convertTime(schedules.get(i).hour, schedules.get(i).minute); //시간 변환
            if( candiTime > temp) { //
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
    void makeSubTree (Node parent) {
        ArrayList<subwayData> possiblePath = getStationDataWithName(parent.data.stationName);   //역 이름으로 경로 탐색
        int i = 0;
        while (i < possiblePath.size()) {   //탐색된 경로의 수만큼
            Node temp = makeNode(possiblePath.get(i));
            if(!deleteDuplicationPath(parent.data, temp.data)) {
                if(parent.data.stationId == temp.data.stationId) {
                    temp.data.transfer = true;
                    dbManager.getTransferDataDB(parent.data.stationDetailId, temp.data.stationDetailId, temp.data.transferInfo);    //환승역 정보 가져옴
                }
                ArrayList<timeTable> schedules = getScheduleData(parent.data, temp.data);
                if (schedules.size() > 0) {
                    //Node child = makeNode(temp.data);
                    int j = 0;
                    while(j < schedules.size()) {
                        temp.data.candiSchedule[j] = schedules.get(j);
                        j++;
                    }
                    updateBestTime(temp.data, schedules);
                    addChild(parent, temp);
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
        newStation.data.stationId = newData.stationId;
        newStation.data.schedule = newData.schedule;
        newStation.data.candiSchedule = newData.candiSchedule;
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

    timeTable getEndTime(subwayData parent, subwayData child, int i) {
        int lineDirection = 0;
        timeTable result = null;
        if(parent.candiSchedule[i].lineDirection == 1) {
            lineDirection = 0;
            result = dbManager.getEndScheduleDataDB(parent, parent.candiSchedule[i].updateId, lineDirection, i);
            int beforeTime = convertTime(result.hour, result.minute);
            result = dbManager.getEndScheduleDataDB(parent, child.stationDetailId, lineDirection, i);
            int afterTime = convertTime(result.hour, result.minute);
            result.numStep = parent.candiSchedule[i].numStep;
            result.transferNum = parent.candiSchedule[i].transferNum;
            result.hour = parent.candiSchedule[i].hour;
            result.minute = parent.candiSchedule[i].minute;
            result.lineDirection = parent.candiSchedule[i].lineDirection;
            result.weekType = parent.candiSchedule[i].weekType;
            result.scheduleName = parent.candiSchedule[i].scheduleName;
            result.typeName = parent.candiSchedule[i].typeName;
            time.calculateTime(parent.candiSchedule[i].hour, afterTime-beforeTime, result);

        }
        else {
            lineDirection = 1;
            result = dbManager.getEndScheduleDataDB(parent, child.stationDetailId, lineDirection, i);
            int beforeTime = convertTime(result.hour, result.minute);
            result = dbManager.getEndScheduleDataDB(parent, parent.candiSchedule[i].updateId, lineDirection, i);
            int afterTime = convertTime(result.hour, result.minute);
            result.numStep = parent.candiSchedule[i].numStep;
            result.transferNum = parent.candiSchedule[i].transferNum;
            result.hour = parent.candiSchedule[i].hour;
            result.minute = parent.candiSchedule[i].minute;
            result.lineDirection = parent.candiSchedule[i].lineDirection;
            result.weekType = parent.candiSchedule[i].weekType;
            result.scheduleName = parent.candiSchedule[i].scheduleName;
            result.typeName = parent.candiSchedule[i].typeName;
            time.calculateTime(parent.candiSchedule[i].hour, afterTime-beforeTime, result);
        }
        return result;
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
                downStation.stationId = temp.stationId;
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
                upStation.stationId = temp.stationId;
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

    ArrayList getOneScheduleData(subwayData parent, subwayData child) {
        ArrayList<timeTable> schedules = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            timeTable schedule;
            if(parent.candiSchedule[i] != null) {
                if (parent.candiSchedule[i].scheduleName.equals(child.stationName)) {   //종착역이면
                    schedule = getEndTime(parent, child, i);    //도착 시간 계산
                    child.candiSchedule[i] = schedule;
                    schedules.add(schedule);
                }
                else {
                    schedule = dbManager.getOneScheduleDataDB(parent, child, i); //parent 이후의 시간에서 child의 시간표를 1개 가져옴
                    try {
                        if(schedule.scheduleName == null) {
                            child.candiSchedule[i] = parent.candiSchedule[i];
                        }
                        else {
                            child.candiSchedule[i] = schedule;
                            child.candiSchedule[i].updateId = child.stationDetailId;
                            schedules.add(schedule);
                        }
                    } catch (IndexOutOfBoundsException e) {    //만약 이후 시간표가 없으면 ex)막차 끊김
                        //System.out.println("열차 없음");
                    }
                }
            }
        }
        return schedules;
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
        subwayData step = new subwayData();
        subwayData previous = parent.data;
        ArrayList<timeTable> schedules = new ArrayList<>();
        int nextStationDetailId = stationDetailId;
        while(true) {
            step = getStationDataWithId(nextStationDetailId);
            step.schedule.lineDirection = 1;
            if(step.stationName.equals(destinationStationName)) {   //목적지
                schedules = getOneScheduleData(previous, step);
                updateBestTime(step, schedules);
                updatePathInfo(previous, step);//목적지
                Node destination = makeNode(step);
                addChild(parent,destination);
                destination.isAlive = false;
                path.add(destination);
                if(step.lineId == root.child.get(0).data.lineId) {
                    finish = true;
                }
                if(path.size() == 3) {
                    finish = true;
                }
                break;
            }
            else {  //목적지 아님
                if (checkTransfer(step.stationName)) {   //환승역
                    if(step.stationName.equals("마곡나루")) {
                        //System.out.println("d");
                    }
                    schedules = getOneScheduleData(previous, step);
                    updateBestTime(step, schedules);
                    updatePathInfo(previous, step);
                    step.transfer = true;
                    addChild(parent, makeNode(step));
                    break;
                }
                else {  //환승역 아님
                    schedules = getOneScheduleData(previous, step);
                    updatePathInfo(previous, step);
                    updateBestTime(step, schedules);
                    if (step.beforeStation == 0) {  //종점
                        Node end = makeNode(step);
                        addChild(parent, end);
                        end.isAlive = false;
                        break;
                    }
                    else {    //종점 아님
                        previous = step;
                        nextStationDetailId = step.beforeStation;
                    }
                    stepPath.push(step);
                }
            }
        }
        return stepPath;
    }

    /*Node searchDownStep(int stationDetailId)
     * nextStation으로 하행인 다음 경로를 탐색 */
    Stack<subwayData> searchDownStep(Node parent, int stationDetailId) {
        Stack<subwayData> stepPath = new Stack<>();
        subwayData step = new subwayData();
        subwayData previous = parent.data;
        ArrayList<timeTable> schedules = new ArrayList<>();
        int nextStationDetailId = stationDetailId;
        while(true) {
            step = getStationDataWithId(nextStationDetailId);
            step.schedule.lineDirection = 0;
            if(step.stationName.equals(destinationStationName)) {   //목적지
                schedules = getOneScheduleData(previous, step);
                updateBestTime(step, schedules);
                updatePathInfo(previous, step);
                Node destination = makeNode(step);
                addChild(parent,destination);
                destination.isAlive = false;
                path.add(destination);
                if(step.lineId == root.child.get(0).data.lineId) {
                    finish = true;
                }
                if(path.size() == 3) {
                    finish = true;
                }
                break;
            }
            else {  //목적지 아님
                if(checkTransfer(step.stationName)) {   //환승역
                    if(step.stationName.equals("마곡나루")) {
                        //System.out.println("d");
                    }
                    schedules = getOneScheduleData(previous, step);
                    updateBestTime(step, schedules);
                    updatePathInfo(previous, step);
                    step.transfer = true;
                    addChild(parent, makeNode(step));
                    break;
                }
                else {  //환승역 아님
                    schedules = getOneScheduleData(previous, step);
                    updatePathInfo(previous, step);
                    updateBestTime(step, schedules);
                    if (step.nextStation == 0) {    //종점
                        Node end = makeNode(step);
                        addChild(parent, end);
                        end.isAlive = false;
                        break;
                    }
                    else {  //종점 아님
                        previous = step;
                        nextStationDetailId = step.nextStation;
                    }
                    stepPath.push(step);
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