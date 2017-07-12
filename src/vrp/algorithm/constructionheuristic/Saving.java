/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vrp.algorithm.constructionheuristic;

/**
 *
 * @author user
 */
public class Saving implements Comparable<Saving> {
     private final int i;
        private final int j;
        private final double saving;

        public Saving(int c1, int c2, double saving) {
            this.i = c1;
            this.j = c2;
            this.saving = saving;
        }

        public int getI() {
            return i;
        }

        public int getJ() {
            return j;
        }

        public double getSaving() {
            return saving;
        }

        @Override
        public int compareTo(Saving o) {
            if (o.getSaving() - saving > 0) {
                return 1;
            } else if (o.getSaving() - saving < 0) {
                return -1;
            }

            return 0;
        }

        @Override
        public String toString() {
            return i + ", " + j + ": " + saving;
        }

}
