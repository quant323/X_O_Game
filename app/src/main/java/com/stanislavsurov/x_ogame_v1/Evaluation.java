package com.stanislavsurov.x_ogame_v1;

public class Evaluation {

    // метод проверки на победу
    public static boolean isWon(String[][] currentState, String sign) {
        String[] lineArrayToCheck = new String[currentState.length];
        // проверка строк
        for (int i = 0; i < currentState.length; i++) {
            // проверка строк
            // поочередно считываем одну из строк и помещаем результат в массив lineArrayToCheck
            for (int j = 0; j < currentState[i].length; j++) {
                lineArrayToCheck[j] = currentState[i][j];
                }
            // проверяем на наличие 3-х одинаковых знаков в строке
            // если условие выполняется, выходим их метода и возвращаем true
            if (LineEvaluation.ifWholeLine(lineArrayToCheck, sign)){
                return true;
            }

            // проверка столбцов
            // поочередно считываем один из столбцов и помещаем результат в массив lineArrayToCheck
            for (int j = 0; j < currentState[i].length; j++) {
                lineArrayToCheck[j] = currentState[j][i];
                }
            // проверяем на наличие 3-х одинаковых знаков в столбце
            // если условие выполняется, выходим их метода и возвращаем true
            if (LineEvaluation.ifWholeLine(lineArrayToCheck, sign)){
                return true;
            }
        }

        // проверка диагоналей
        // диагональ левая
        for (int i = 0; i < 3; i++){
            lineArrayToCheck[i] = currentState[i][i];
        }
        // проверяем на наличие 3-х одинаковых знаков в диагонали
        // если условие выполняется, выходим их метода и возвращаем true
        if (LineEvaluation.ifWholeLine(lineArrayToCheck, sign)) {
            return true;
        }

        // диагональ правая
        for (int i = 0, j = 2; i < 3; i++, j--){
            lineArrayToCheck[i] = currentState[i][j];
        }
        // проверяем на наличие 3-х одинаковых знаков в диагонали
        // если условие выполняется, выходим их метода и возвращаем true
        if (LineEvaluation.ifWholeLine(lineArrayToCheck, sign)) {
            return true;
        }

        // если ни одно из вышеуказанных условий не выполняется - возвращаем false
        return false;
    }


    // метод поиска свободных полей
    public static boolean areThereEmptyFields(String[][] currentState, String sign) {
        for (int i = 0; i < currentState.length; i++) {
            for (int j = 0; j < currentState.length; j++) {
                if (currentState[i][j].equals(sign)) {
                    return true;
                }
            }
        }
        return false;
    }

}



