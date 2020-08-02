package com.stanislavsurov.x_ogame_v1;

public class Controller {

    private final String mPlayerOneSign;   // знак игрока (Х или О)
    private final String mPlayerTwoSign;      // знак компьютера (Х или О)
    private final String mEmptySign;  // знак пустого поля

    /**
     * Конструстор класса
     * @param playerOneSign знак игрока (Х или О)
     * @param playerTwoSign знак компьютера (Х или О)
     * @param emptySign знак пустого поля
     */
    public Controller(String playerOneSign, String playerTwoSign, String emptySign) {
        mPlayerOneSign = playerOneSign;
        mPlayerTwoSign = playerTwoSign;
        mEmptySign = MainActivity.EMPTY_SIGN;
    }



    /**
     * Проверяет на победу и на наличие пустых строк
     *
     * @param currentState массив текущего состояния полей
     * @return  значения от 0 до 4-х, где:
     *      1 - победа игрока 1;
     *      2 - победа игрока 2;
     *      3 - нет пустых полей;
     *      0 - ни одно из перечисленных условий не выполнено
     */
    public int evaluateState(String playerOneSign, String playerTwoSign, String[][] currentState){
        if(Evaluation.isWon(currentState, playerOneSign)){
            return 1;
        } else if (Evaluation.isWon(currentState, playerTwoSign)){
            return 2;
        } else if (!Evaluation.areThereEmptyFields(currentState, mEmptySign)){
            return 3;
        } else return 0;
    }


    /**
     * просчет хода компьютера
     * @param currentState - текущее состояние полей
     * @return - координаты хода компьютера
     */
    public int[] pcTurn(String playerOneSign, String playerTwoSign, String[][] currentState){
        PC_Mind pc_mind_2 = new PC_Mind(playerOneSign, playerTwoSign);
        pc_mind_2.setCurrentState(currentState);    // передаем текущее состояние полей
        return pc_mind_2.nextTurnCalculation();     // возвращаем координаты хода компьютера
    }
}
