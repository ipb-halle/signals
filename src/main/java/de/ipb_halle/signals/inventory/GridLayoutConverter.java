/*
 *
 *  * IPB Signals client
 *  * Copyright 2024 Leibniz-Institut f. Pflanzenbiochemie
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *
 */

package de.ipb_halle.signals.inventory;

public class GridLayoutConverter {

    public static char[][] convertGridStringTo2DArray(String gridString, int rows, int cols) {
        char[][] grid = new char[rows][cols];
        for (int i = 0; i < gridString.length(); i++) {
            grid[i / cols][i % cols] = gridString.charAt(i);
        }
        return grid;
    }

    public static String convert2DArrayToGridString(char[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            for (char c : row) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static char[][] generateGrid(String... occupiedPositions) {
        char[][] grid = new char[8][12];

        //set all fields as default on Empty 'E';
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = 'E';
            }
        }

        // set position which are occupied = 'C';
        for (String position : occupiedPositions) {
            if (position.length() < 2 || position.length() > 3) {
                throw new IllegalArgumentException("GridLayoutConverter:-> Invalid grid position: " + position);
            }

            char rowChar = Character.toLowerCase(position.charAt(0));
            int row = rowChar - 'a';

            int col;
            try {
                col = Integer.parseInt(position.substring(1)) - 1;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("GridLayoutConverter:-> Invalid column number: " + position);
            }
            if (row < 0 || row >= 8 || col < 0 || col >= 12) {
                throw new IllegalArgumentException("GridLayoutConverter:-> Position out of bounds: " + position);
            }

            grid[row][col] = 'C';
        }
        return grid;
    }

    public static void printGrid(char[][] grid) {
        for (char[] row : grid) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        char[][] newArr = convertGridStringTo2DArray("CCEEEEEEEEEECCEEEEEEEEEECEEEEEEEEEEECEEEEEEEEEEECEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEECCCCCCCEEEEE", 8, 12);
        printGrid(newArr);
        System.out.println("========================");

        char[][] grid = generateGrid("A1", "B8", "C10");
        printGrid(grid);

        String gridString = convert2DArrayToGridString(grid);
        System.out.println("========================");
        System.out.println(gridString);

    }
}
