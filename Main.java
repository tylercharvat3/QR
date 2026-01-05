public class Main {
    public static void main(String[] args) {
        // System.out.println("Working");
        // System.out.println(QR.GenerateQRCode("Hello, world!")); // expected 1011

        // int[][] newList = new int[21][21];
        // newList = QR.AddFinderPattern(newList, 0, 0);
        // newList = QR.AddFinderPattern(newList, 14, 0);
        // newList = QR.AddFinderPattern(newList, 0, 14);
        // newList = QR.AddTimingPattern(newList);
        int[][] qrCode = QR.GenerateQRCode("https://1.com");
        for (int i = 0; i < qrCode.length; i++) {
            for (int j = 0; j < qrCode[i].length; j++) {
                System.out.print(qrCode[i][j]);
            }
            System.out.println();
        }
    }
}
