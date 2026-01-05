import java.nio.charset.StandardCharsets;

public class QR {

    // Generate QR (Level 1-L) and return ECC bytes
    public static int[][] GenerateQRCode(String data) {
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

        String BinaryList = ConvertToBinary(finalList);
        System.out.println(BinaryList);

        int[][] qrCode = QRCodePattern(finalList, BinaryList);

        return qrCode;
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

    public static int[][] QRCodePattern(int[] finalList, String dataString) {
        // 0 is white, 1 is black, 2 is reserved white, 3 is reserved unknown
        int[][] pattern = new int[21][21];

        pattern = AddFinderPattern(pattern, 0, 0);
        pattern = AddFinderPattern(pattern, 14, 0);
        pattern = AddFinderPattern(pattern, 0, 14);

        pattern = AddTimingPattern(pattern);

        // FINDER PATTERN AND TIMING PATTERNS ADDED

        pattern = AddFormatInformation(pattern);

        pattern = AddData(pattern, dataString);

        return pattern;
    }

    public static int[][] AddFinderPattern(int[][] inList, int xIndex, int yIndex) {
        int[][] finalList = inList;
        if (xIndex > 14) {
            System.out.println("Too far right");
        }
        if (yIndex > 14) {
            System.out.println("Too far down");
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (i == 0 || i == 6) {
                    finalList[j + yIndex][i + xIndex] = 1;
                } else if (j == 0 || j == 6) {
                    finalList[j + yIndex][i + xIndex] = 1;
                } else if (i > 1 && i < 5 && j > 1 && j < 5) {
                    finalList[j + yIndex][i + xIndex] = 1;
                } else {
                    finalList[j + yIndex][i + xIndex] = 2;
                }
            }
        }
        // outside finder pattern is 2, not 0
        for (int i = 0; i < 8; i++) {
            if (xIndex == 0) {
                // left side
                if (yIndex > 0) {
                    // bottom left
                    finalList[i][yIndex - 1] = 2;
                    if (i == 7) {
                        for (int j = 0; j < 8; j++) {
                            finalList[7][yIndex - 1 + j] = 2;
                        }
                    }
                } else {
                    // top left
                    finalList[i][yIndex + 7] = 2;
                    if (i == 7) {
                        for (int j = 0; j < 8; j++) {
                            finalList[7][yIndex + 7 - j] = 2;
                        }
                    }
                }
            } else {
                // top right
                finalList[xIndex + i - 1][yIndex + 7] = 2;
                if (i == 7) {
                    for (int j = 0; j < 8; j++) {
                        finalList[xIndex - 1][j] = 2;
                        System.out.println("Setting " + xIndex + "," + j + " to 2");
                    }
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

        // dark module
        finalList[8][13] = 1;

        return finalList;
    }

    public static int[][] AddFormatInformation(int[][] inList) {
        int[][] finalList = inList;
        // To be implemented
        // top left
        for (int i = 0; i < 9; i++) {
            if (i != 6) {
                finalList[8][i] = 3; // horizontal
                finalList[i][8] = 3; // vertical
            }
        }
        // top right
        for (int i = 0; i < 8; i++) {
            finalList[i + 13][8] = 3;
        }
        // bottom left
        for (int i = 0; i < 7; i++) {
            finalList[8][i + 14] = 3;
        }
        return finalList;
    }

    public static int[][] AddData(int[][] inList, String dataString) {
        int[][] finalList = inList;
        int dataIndex = 0;

        // Start from rightmost column pair
        int col = 20;
        boolean goingUp = true;

        while (col >= 0 && dataIndex < dataString.length()) {
            // Skip column 6 (vertical timing pattern)
            if (col == 6) {
                col--;
                continue;
            }

            // Process 2-column pair
            for (int row = 0; row < 21; row++) {
                int currentRow = goingUp ? (20 - row) : row;

                // Right column of the pair
                if (finalList[currentRow][col] == 0) {
                    finalList[currentRow][col] = Character.getNumericValue(dataString.charAt(dataIndex));
                    dataIndex++;
                    if (dataIndex >= dataString.length())
                        return finalList;
                }

                // Left column of the pair
                if (finalList[currentRow][col - 1] == 0) {
                    finalList[currentRow][col - 1] = Character.getNumericValue(dataString.charAt(dataIndex));
                    dataIndex++;
                    if (dataIndex >= dataString.length())
                        return finalList;
                }
            }

            // Move to next column pair and flip direction
            col -= 2;
            goingUp = !goingUp;
        }

        return finalList;
    }
}
