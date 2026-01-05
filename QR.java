import java.io.File;
import java.nio.charset.StandardCharsets;

public class QR {

    // Generate QR (Level 1-L) and return ECC bytes
    public static File GenerateQRCode(String data) {
        System.out.println("Testing MUL: 64x1=" + Galois.Mul(64, 1));
        String rawDataBits = GetRawDataBits(data);

        int dataBytes = rawDataBits.length() / 8;
        int[] messageBytes = new int[dataBytes + 7]; // +7 for error correction

        for (int i = 0; i < dataBytes; i++) {
            messageBytes[i] = Integer.parseInt(rawDataBits.substring(i * 8, i * 8 + 8), 2);
        }

        int[] eccBytes = GetECCDataBytes(messageBytes);
        System.out.print("ECC Bytes: ");
        PrintArray(eccBytes);

        // No need to interleave codewords, 1 block for message and 1 for ecc
        int[] finalList = CombineArrays(messageBytes, eccBytes);
        PrintArray(finalList);

        /*
         * String BinaryList = ConvertToBinary(finalList);
         * System.out.println(BinaryList);
         */

        return new File("C:/");
    }

    // Convert data to raw bits
    public static String GetRawDataBits(String data) {
        int charCount = data.length();
        StringBuilder totalString = new StringBuilder();

        // Mode indicator (byte mode)
        totalString.append("0100");

        // Character count (9 bits)
        String byteCharCount = padByteString(Integer.toBinaryString(charCount), 9);
        totalString.append(byteCharCount);

        // Data bytes in ISO-8859-1
        byte[] isoBytes = data.getBytes(StandardCharsets.ISO_8859_1);
        for (byte b : isoBytes) {
            totalString.append(padByteString(Integer.toBinaryString(b & 0xFF), 8));
        }

        // Terminator (up to 4 zeros)
        int bitsToGo = 152 - totalString.length();
        totalString.append("0".repeat(Math.max(0, Math.min(4, bitsToGo))));

        // Pad to multiple of 8 bits
        while (totalString.length() % 8 != 0)
            totalString.append("0");

        // Add pad bytes (alternating 0xEC and 0x11)
        int padBytesToGo = (152 - totalString.length()) / 8;
        for (int i = 0; i < padBytesToGo; i++) {
            totalString.append((i % 2 == 0) ? "11101110" : "00010001");
        }

        return totalString.toString();
    }

    // Calculate ECC bytes
    public static int[] GetECCDataBytes(int[] messageBytes) {
        // QR Version 1-L generator polynomial (degree 7)
        int[] generator = { 1, 127, 122, 154, 164, 11, 68, 117 };

        // Polynomial division to get remainder
        return Galois.DividePolynomials(messageBytes, generator);
    }

    // Pad binary string to expected length
    public static String padByteString(String bits, int expectedSize) {
        while (bits.length() < expectedSize)
            bits = "0" + bits;
        return bits;
    }

    // Print int array
    public static void PrintArray(int[] arr) {
        System.out.print("{");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i]);
            if (i < arr.length - 1)
                System.out.print(", ");
        }
        System.out.println("}");
    }

    public static int[] CombineArrays(int[] arr1, int[] arr2) {
        int[] finalArr = new int[arr1.length + arr2.length];
        for (int i = 0; i < arr1.length; i++) {
            finalArr[i] = arr1[i];
        }
        for (int i = 0; i < arr2.length; i++) {
            finalArr[i + arr1.length] = arr2[i];
        }

        return finalArr;
    }

    public static String ConvertToBinary(int[] data) {
        String finalStr = "";
        for (int i = 0; i < data.length; i++) {
            finalStr += padByteString(Integer.toBinaryString(data[i]), 8);
        }
        return finalStr;
    }

    public static int[][] QRCodePattern(int[] finalList) {
        int[][] pattern = new int[21][21];

        pattern = AddFinderPattern(pattern, 0, 0);
        pattern = AddFinderPattern(pattern, 12, 0);
        pattern = AddFinderPattern(pattern, 0, 12);

        pattern = AddTimingPattern(pattern);

        // FINDER PATTERN AND TIMING PATTERNS ADDED

        return pattern;
    }

    public static int[][] AddFinderPattern(int[][] inList, int xIndex, int yIndex) {
        int[][] finalList = inList;
        if (xIndex > 12) {
            System.out.println("Too far right");
        }
        if (yIndex > 12) {
            System.out.println("Too far down");
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (i == 1 || i == 7) {
                    finalList[j + yIndex][i + xIndex] = 1;
                }
                if (j == 1 || j == 7) {
                    finalList[j + yIndex][i + xIndex] = 1;
                }
                if (i > 2 && i < 6 && j > 2 && j < 6) {
                    finalList[j + yIndex][i + xIndex] = 1;
                }
                if (i == 0 || i == 8 || j == 0 || j == 8) {
                    finalList[j + yIndex][i + xIndex] = 2;
                }
            }
        }
        return finalList;
    }

    public static int[][] AddTimingPattern(int[][] inList) {
        int[][] finalList = inList;
        finalList[8][6] = 1;// 8, 10, 12
        finalList[10][6] = 1;// 8, 10, 12
        finalList[12][6] = 1;// 8, 10, 12

        finalList[6][8] = 1;
        finalList[6][10] = 1;
        finalList[6][12] = 1;

        finalList[8][13] = 1;

        return finalList;
    }
}
