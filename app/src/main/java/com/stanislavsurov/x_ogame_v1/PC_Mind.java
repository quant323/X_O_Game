package com.stanislavsurov.x_ogame_v1;

import java.lang.reflect.Array;
import java.util.Random;

public class PC_Mind {

    private String LOG_TAG = "myLog_PC_Mind_2";
    private String[][] mCurrentState;   // текущее состояние полей
    private final String mPlayerSign;   // знак игрока (Х или О)
    private final String mPC_Sign;      // знак компьютера (Х или О)
    private final String EMPTY_SIGN = MainActivity.EMPTY_SIGN;  // знак пустого поля
    private int[] mTurnsToMake = new int[2];     // координаты поля для хода компьютера

    /**
     * Конструктор класса
     * @param playerSign знак игрока
     * @param PC_Sign знак компьютера
     */
    public PC_Mind(String playerSign, String PC_Sign){
        mPlayerSign = playerSign;
        mPC_Sign = PC_Sign;
    }

    /**
     * Получение текущего состояния полей
     * @param currentState массив текущего состояния полей
     */
    public void setCurrentState(String[][] currentState){
        mCurrentState = currentState;
    }

    /**
     * Просчет следующего хода компьютера
     * @return массив координат поля для хода, состоящий из 2-х элементов
     */
    public int[] nextTurnCalculation(){
        //проверка 2 из 3-х (компьютер)
        if(twoOfThreeCheck(mPC_Sign)){
            return mTurnsToMake;
            //проверка 2 из 3-х (игрок)
        } else if (twoOfThreeCheck(mPlayerSign)){
            return mTurnsToMake;
        } else {
            // рандомный ход
            randomTurn();
            return mTurnsToMake;
        }
    }

    /**
     * проверка всех полей на 2 из 3-х в одном ряду
     * @param sign знак для проверки (Х или О)
     * @return имеется ли случай 2 из 3-х
     */
    private boolean twoOfThreeCheck(String sign){
        // массив полей в одной строке/столбце/диагонали
        int returnedCoordinate;
        String[] lineArrayToCheck = new String[mCurrentState.length];

        for (int i = 0; i < mCurrentState.length; i++) {
            // проверка строк
            // поочередно считываем одну из строк и помещаем результат в массив lineArrayToCheck
            for (int j = 0; j < mCurrentState[i].length; j++) {
                lineArrayToCheck[j] = mCurrentState[i][j];
            }
            // проверяем на 2 из 3-х; если условие проверки верно - устанавливаем
            // координаты для следующего хода компьютера и выходим из метода
            returnedCoordinate = LineEvaluation.searchOneInLine(lineArrayToCheck, sign, EMPTY_SIGN);
            if (returnedCoordinate != -1){
                setCoordinates(i, returnedCoordinate);
                return true;
            }

            // проверка столбцов
            // поочередно считываем один из столбцов и помещаем результат в массив lineArrayToCheck
            for (int j = 0; j < mCurrentState[i].length; j++) {
                lineArrayToCheck[j] = mCurrentState[j][i];
            }
            // проверяем на 2 из 3-х; если условие проверки верно - устанавливаем
            // координаты для следующего хода компьютера и выходим из метода
            returnedCoordinate = LineEvaluation.searchOneInLine(lineArrayToCheck, sign, EMPTY_SIGN);
            if (returnedCoordinate != -1){
                setCoordinates(returnedCoordinate, i);
                return true;
            }
        }

        // проверка диагоналей
        // диагональ левая
        for (int i = 0; i < 3; i++){
            lineArrayToCheck[i] = mCurrentState[i][i];
        }
        // проверяем на 2 из 3-х; если условие проверки верно - устанавливаем
        // координаты для следующего хода компьютера и выходим из метода
        returnedCoordinate = LineEvaluation.searchOneInLine(lineArrayToCheck, sign, EMPTY_SIGN);
        if (returnedCoordinate != -1) {
            setCoordinates(returnedCoordinate, returnedCoordinate);
            return true;
        }

        // диагональ правая
        for (int i = 0, j = 2; i < 3; i++, j--){
            lineArrayToCheck[i] = mCurrentState[i][j];
        }
        // проверяем на 2 из 3-х; если условие проверки верно - устанавливаем
        // координаты для следующего хода компьютера и выходим из метода
        returnedCoordinate = LineEvaluation.searchOneInLine(lineArrayToCheck, sign, EMPTY_SIGN);
        if (returnedCoordinate != -1) {
            switch (returnedCoordinate){          // выбор координаты 2 по условию от координаты 1
                case 0:
                    setCoordinates(returnedCoordinate, 2);
                    break;
                case 1:
                    setCoordinates(returnedCoordinate, 1);
                    break;
                case 2:
                    setCoordinates(returnedCoordinate, 0);
                    break;
            }
            return true;
        }
        // возвращаем false, если отсутствуют случаи 2 из 3-х
        return false;
    }

    /**
     * Рандомный ход
     */
    private void randomTurn(){
        int line;
        int column;
        Random random = new Random();
        boolean condition = false;

        if (mCurrentState[1][1].equals(EMPTY_SIGN)){
            setCoordinates(1, 1);
            return;
        }

        // запускаем random, пока не наткнемся на пустую клетку
        while(!condition){
            line = random.nextInt(3);
            column = random.nextInt(3);
            // при нахождении пустой строки - устанавливаем
            // координаты для следующего хода компьютера и выходим из цикла
            if (mCurrentState[line][column].equals(MainActivity.EMPTY_SIGN)){
                setCoordinates(line, column);
                condition = true;
            }
        }
    }


    /**
     * Set coordinates for the PC next turn
     * @param coordinateOne - первая координата для хода
     * @param coordinateTwo - вторая координата для хода
     */
    private void setCoordinates(int coordinateOne, int coordinateTwo){
        mTurnsToMake[0] = coordinateOne;
        mTurnsToMake[1] = coordinateTwo;
    }
}
