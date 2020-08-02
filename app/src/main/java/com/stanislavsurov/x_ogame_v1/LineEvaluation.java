package com.stanislavsurov.x_ogame_v1;

import android.util.Log;

import java.util.Arrays;

public class LineEvaluation {

    private static final String LOG_TAG = "myLog_LineEvaluation";

    /**
     * Метод проверяет, имеется ли в данном массиве случай,
     * когда все элементы, кроме одного - normalSign, а один - signToSearch.
     * Метод принимает на вход массив из строк. В этом массиве могут быть строки только 2-х
     * типов: normalSing и signToSearch.
     * @param arrayToCheck  массив из строк для проверки
     * @param normalSign   знак большинства элементов
     * @param signToSearch знак, для поиска одного в ряду
     * @return индекс элемента массива, содержащий signToSearch, есль условие поиска выполнено;
     * если условие поиска не выролнено - возвращает -1
     */
    public static int searchOneInLine(String[] arrayToCheck, String normalSign, String signToSearch) {
        // инициализируем массив, заполняя все его элементы знаками по умолчанию
        // данный массив будет использоваться для сравнения его элементов с массивом,
        // поступившим на вход метода
        String[] signArray = new String[arrayToCheck.length];
        for (int i =0; i < arrayToCheck.length; i++) {
            signArray[i] = normalSign;
        }

        // устанавливаем в первый элемент массива signArray знак для поиска, и сравниваем
        // полученный массив с массивом, полученным на вход;
        // если условие выполняется (массивы одинаковы) - возвращаем текущий индекс элемента
        // массива signArray, если нет - обратно устанавливаем в текущий элемент массива
        // normalSign, а signToSearch - в следующий элемент массива.
        // если при переборе всех элементов массива условие не выполняется - возвращаем -1
        for (int c = 0; c < arrayToCheck.length; c++) {
            signArray[c] = signToSearch;
           if (Arrays.equals(signArray, arrayToCheck))
               return c;
            signArray[c] = normalSign;
        }
        return -1;
    }


    /**
     * Метод проверяет, состоит ли массив, поданный на вход, только из элементов,
     * определенных в переменной sign.
     * @param arrayToCheck массив для проверки
     * @param sign знак для проверки массива
     * @return true - если массив состоит только из элементов, определенных переменной sing;
     * false - в остальных случаях
     */
    public static boolean ifWholeLine(String[] arrayToCheck, String sign) {
        // инициализируем массив, заполняя все его элементы знаком sign, поступившем на вход
        String[] signArray = new String[arrayToCheck.length];
        for (int i =0; i < arrayToCheck.length; i++) {
            signArray[i] = sign;
        }

        // сравниваем проинициализтрованный массив с массивом, поступившем на вход;
        // если массивы равны, возвращаем true, если нет - false
        if (Arrays.equals(signArray, arrayToCheck))
            return true;
        else
            return false;
    }

}
