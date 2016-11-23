/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utility;

import java.math.BigDecimal;

/**
 *
 * @author osman
 */
public class Util {
    public static double applyPrecision(double number, int precision) {
        try {
            if (number == Double.POSITIVE_INFINITY || number == Double.NEGATIVE_INFINITY) {
                return number;
            }
            BigDecimal bd = new BigDecimal(number);
            bd = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
            return bd.doubleValue();
        } catch (Exception e) {
            return number;
        }
    }
}
