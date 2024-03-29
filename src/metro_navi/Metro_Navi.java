package metro_navi;

import java.util.*;

/*역 정보*/
class subwayData {

    subwayData() {}
    subwayData(int SI, String SN, String SC, int SDI, int LI, int BS, int NS) {
        stationId = SI;
        stationName = SN;
        stationCode = SC;
        stationDetailId = SDI;
        lineId = LI;
        beforeStation = BS;
        nextStation = NS;
    }


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

    transfer transferInfo = new transfer();
}

/*환승역 정보*/
class transfer {
    int startDetailId;
    int finishDetailId;
    int distance;
    int timeSec;
}

/*열차 시간표*/
class timeTable {
    int lineDirection;  //진행 방향
    int hour;   //출발 시각(시)
    int minute; //출발 시각(분)
    String weekType;    //요일
    String scheduleName;    //종착 지점
    String typeName;    //열차 종류
    int duration;   //소요 시간
    int transferNum;    //환승 횟수
    int numStep;    //정류장 수

    float congest;  //혼잡도

    int congesetScore;  //혼잡도 환산 점수
    int updateId;   //시간표 바꾼 역 station_detail_id
}

/*노드*/
class Node {
    boolean isAlive = true;    //살아있는지 판단
    subwayData data = new subwayData(); //데이터
    Node beforeNode;   //부모 노드
    ArrayList<Node> child = new ArrayList<Node>();    //자식 노드
    Stack<subwayData> step = new Stack<subwayData>();   //중간 정류장
}




public class Metro_Navi {
    public static void main(String[] args) {
        databaseManager.connectDatabase();
        ArrayList<subwayData> subData = new ArrayList<subwayData>();
        String departureStationName;
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

        for(int i = 0; i < tree.path.size(); i++) {
            Node temp;
            subwayData temp1;
            temp = tree.path.get(i);

            System.out.println("\"ShortestPath\" : [");
            System.out.println("    {");
            System.out.print("        \"stationName\" : \"" + temp.data.stationName + "\",\n" + "        \"stationCode\" : \"" + temp.data.stationCode + "\",\n" + "        \"lineId\" : \"" + temp.data.lineId + "\",\n" + "        \"schedule\" : {\n");
            System.out.print("            \"lineDirection\" : \"" + temp.data.schedule.lineDirection + "\",\n" + "            \"weekType\" : \"" + temp.data.schedule.weekType + "\",\n" + "            \"hour\" : " + temp.data.schedule.hour + ",\n" +
                    "            \"minute\" : " + temp.data.schedule.minute + ",\n" + "            \"typeName\" : \"" + temp.data.schedule.typeName + "\",\n" + "            \"scheduleName\" : \"" + temp.data.schedule.scheduleName + "\",\n" + "            \"congest\" : \"" + temp.data.schedule.congest + "\",\n"
                    + "        }\n" + "        \"transfer\" : {\n");
            System.out.print("            \"isTranfer\" : " + temp.data.transfer + ",\n" + "            \"transferDistance\" : " + temp.data.transferInfo.distance + ",\n            \"transferTime\" : " + temp.data.transferInfo.timeSec + "\n        }\n    }\n");
            while (true) {
                if (temp.beforeNode != null) {
                    if (temp.beforeNode.beforeNode == null) {
                        System.out.println("출발 : " + tree.root.data.schedule.hour + "시 " + tree.root.data.schedule.minute + "분\n");
                        break;
                    } else {
                        if (temp.data.stationId != temp.beforeNode.data.stationId) {
                            for (subwayData data : temp.beforeNode.step) {
                                System.out.println("    {");
                                System.out.print("        \"stationName\" : \"" + data.stationName + "\",\n" + "        \"stationCode\" : \"" + data.stationCode + "\",\n" + "        \"lineId\" : \"" + data.lineId + "\",\n" + "        \"schedule\" : {\n");
                                System.out.print("            \"lineDirection\" : \"" + data.schedule.lineDirection + "\",\n" + "            \"weekType\" : \"" + data.schedule.weekType + "\",\n" + "            \"hour\" : " + data.schedule.hour + ",\n" +
                                        "            \"minute\" : " + data.schedule.minute + ",\n" + "            \"typeName\" : \"" + data.schedule.typeName + "\",\n" + "            \"scheduleName\" : \"" + data.schedule.scheduleName + "\",\n" + "            \"congest\" : \"" + data.schedule.congest + "\",\n"
                                        + "        }\n" + "        \"transfer\" : {\n");
                                System.out.print("            \"isTranfer\" : " + data.transfer + ",\n" + "            \"transferDistance\" : " + data.transferInfo.distance + ",\n            \"transferTime\" : " + data.transferInfo.timeSec + "\n        }\n    }\n");
                            }

                            temp = temp.beforeNode;
                            System.out.println("    {");
                            System.out.print("        \"stationName\" : \"" + temp.data.stationName + "\",\n" + "        \"stationCode\" : \"" + temp.data.stationCode + "\",\n" + "        \"lineId\" : \"" + temp.data.lineId + "\",\n" + "        \"schedule\" : {\n");
                            System.out.print("            \"lineDirection\" : \"" + temp.data.schedule.lineDirection + "\",\n" + "            \"weekType\" : \"" + temp.data.schedule.weekType + "\",\n" + "            \"hour\" : " + temp.data.schedule.hour + ",\n" +
                                    "            \"minute\" : " + temp.data.schedule.minute + ",\n" + "            \"typeName\" : \"" + temp.data.schedule.typeName + "\",\n" + "            \"scheduleName\" : \"" + temp.data.schedule.scheduleName + "\",\n" + "            \"congest\" : \"" + temp.data.schedule.congest + "\",\n"
                                    + "        }\n" + "        \"transfer\" : {\n");
                            System.out.print("            \"isTranfer\" : " + temp.data.transfer + ",\n" + "            \"transferDistance\" : " + temp.data.transferInfo.distance + ",\n            \"transferTime\" : " + temp.data.transferInfo.timeSec + "\n        }\n    }\n");
                            for (subwayData data : temp.beforeNode.step) {
                                System.out.println("    {");
                                System.out.print("        \"stationName\" : \"" + data.stationName + "\",\n" + "        \"stationCode\" : \"" + data.stationCode + "\",\n" + "        \"lineId\" : \"" + data.lineId + "\",\n" + "        \"schedule\" : {\n");
                                System.out.print("            \"lineDirection\" : \"" + data.schedule.lineDirection + "\",\n" + "            \"weekType\" : \"" + data.schedule.weekType + "\",\n" + "            \"hour\" : " + data.schedule.hour + ",\n" +
                                        "            \"minute\" : " + data.schedule.minute + ",\n" + "            \"typeName\" : \"" + data.schedule.typeName + "\",\n" + "            \"scheduleName\" : \"" + data.schedule.scheduleName + "\",\n" + "            \"congest\" : \"" + data.schedule.congest + "\",\n"
                                        + "        }\n" + "        \"transfer\" : {\n");
                                System.out.print("            \"isTranfer\" : " + data.transfer + ",\n" + "            \"transferDistance\" : " + data.transferInfo.distance + ",\n            \"transferTime\" : " + data.transferInfo.timeSec + "\n        }\n    }\n");
                            }
                        } else {
                            temp = temp.beforeNode;

                        }
                    }
                } else {
                    System.out.println("\n경로 " + i + "\n");
                    break;
                }
            }
        }
    }
/*        //subData = dbManager.getStationData(departureStaionName);
        subData = tree.getStationData(departureStaionName);*/
}