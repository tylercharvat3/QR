public class Main {
    public static void main(String[] args) {
        // System.out.println("Working");
        // System.out.println(QR.GenerateQRCode("Hello, world!")); // expected 1011

        // int[][] newList = new int[21][21];
        // newList = QR.AddFinderPattern(newList, 0, 0);
        // newList = QR.AddFinderPattern(newList, 14, 0);
        // newList = QR.AddFinderPattern(newList, 0, 14);
        // newList = QR.AddTimingPattern(newList);
        int[][] newList = QR.QRCodePattern(new int[21]);
        for (int i = 0; i < 21; i++) {
            for (int j = 0; j < 21; j++) {
                System.out.print(newList[j][i]);
            }
            System.out.println();
        }
    }
}
