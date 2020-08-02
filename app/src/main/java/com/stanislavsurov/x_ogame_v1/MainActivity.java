package com.stanislavsurov.x_ogame_v1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    public static final String LOG_TAG = "myLog_MainActivity";

    public static final String X_SIGN = "X";
    public static final String O_SIGN = "O";
    public static final String EMPTY_SIGN = ".";

    private boolean mIsGameFinished;            // закончена ли игра

    private int mPlayerOneScore;                // кол-во побед игрока 1
    private int mPlayerTwoScore;                // кол-во побед игрока 2
    private int mDraw;                          // кол-во ничьих
    private int mGameMode;                      // тип игры

    private String mYourSign;                   // знак игрока 1
    private String mOpponentSign;               // знак игрока 2
    private String mPlayerOneName;              // имя игрока 1
    private String mPlayerTwoName;              // имя игрока 2
    private String mSignForPvP;                 // знак для Player vs Player
    private String mPlayerNameForNetwork;       // имя игрока для подстановки в сообщение при сетевой игре

    private Button[][] mFields;                 // массив полей
    private String[][] mCurrentState;           // массив текущего состояния полей

    private TextView resultTextView;            // сообщение с результатом
    private TextView playerOneScoreTextView;    // кол-во побед игрока 1
    private TextView playerTwoScoreTextView;    // кол-во побед игрока 2
    private TextView drawTextView;              // кол-во ничьих
    private TextView onLineTextView;            // состояние Online/Offline

    private Controller mController;             // ссылка на объект класса "Контроллер"

    // для игры по сети
    private EditText textToTest_EditText;       // поле для ввода значений
    private boolean mIsPlayerOne;               // игрок 1
    private boolean mIsPlayerTwo;               // игрок 2
    private boolean mPermissionToTurn;          // разрешение на ход
    private boolean mIsAssign;                  // определен ли ранг игрока (это игрок 1 или игрок 2)

    private static final String STAND_BY = "Stand_By";
    private static final String REQUEST_SENT = "Request_Sent";
    private static final String REQUEST_RECEIVED = "Request_Received";
    private static final String TURN_RECEIVED = "Turn_Received";
    private static final String WAITING_FOR_THE_TURN = "Waiting_for_the_Turn";
    private static final String READY_TO_START = "Ready_to_Start";
    private static final String PLAYER_1 = "Player1_";
    private static final String PLAYER_2 = "Player2_";
    private static final String COORDINATES = "Coordinates";
    private static final String CLEAN_UP = "Clean_Up";
    private static final String ONLINE = "player 2 onLine";
    private static final String OFFLINE = "offLine";
    private static final String SEARCHING = "Searching for the player 2";

    private static final String YOUR_TURN = "Your Turn";
    private static final String OPPONENT_TURNS = "Opponent Turns";

    // объекты для базы данных
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // инициализируем текстовые поля
        resultTextView = findViewById(R.id.textView_result);
        playerOneScoreTextView = findViewById(R.id.you_score_textView);
        playerTwoScoreTextView = findViewById(R.id.pc_score_textView);
        drawTextView = findViewById(R.id.draw_textView);
        onLineTextView = findViewById(R.id.online_textView);

        // для теста
        textToTest_EditText = findViewById(R.id.text_to_test);
        mGameMode = 0;

        // инициализируем данные для игроков при первом запуске
        playersInit();

        // инициализируем массив полей
        mFields = fieldsInit();

        // создаем объект класса Controller
        mController = new Controller(mYourSign, mOpponentSign, EMPTY_SIGN);

        // инициализация и работа с FireBase
//        fireBaseInit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // при игре через интернет, при выходе, записываем в FireBase, что игрок offline
        if (mGameMode == 4) {
            sendOfflineMessage();
            makeToast(this, "onStop");
            // устанавливаем надпись "offLine"
            onLineTextView.setText(OFFLINE);
        }
    }

    /**
     * Inflates the menu, and adds items to the action bar if it is present.
     *
     * @param menu Menu to inflate.
     * @return Returns true if the menu inflated.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Обрабатываем нажатие пунктов меню
     *
     * @param item Item clicked.
     * @return True if one of the defined items was clicked.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_game_mode:
                // показываем диалоговое окно выбора типа игры
                createGameModeDialog();
                return true;
            case R.id.action_settings:
                makeToast(this,"Settings");
                return true;
            default:
                // Do nothing
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Создание диалогового окна "Game Mode"
     */
    private void createGameModeDialog() {
        // присваиваем временной переменной текущее значение mGameMode
        final int tempGameMode = mGameMode;

        // создаем массив для пунктов меню выбора
        final String[] gameModes = {"player vs PC", "PC vs PC", "Player vs Player",
                "Player vs Player (Host)", "Player vs Player (Internet)"};

        // создаем новый AlertDialog
        AlertDialog.Builder gameModeDialog = new AlertDialog.Builder(MainActivity.this);

        // устанавливаем заголовок
        gameModeDialog.setTitle("Game Mode");

        // устанавливаем слушатель на кнопки меню; устанавливаем переключатель текущего mGameMode;
        // параметр which - номер нажатого элемента
        gameModeDialog.setSingleChoiceItems(gameModes, mGameMode, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // устанавливаем выбранный тип игры
                mGameMode = which;
            }
        });

        // нажатие на кнопку "ОК"
        gameModeDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ничего не делаем, оставляем выбранный тип игры
                Toast.makeText(getApplicationContext(), gameModes[mGameMode] + " is chosen",
                        Toast.LENGTH_SHORT).show();
                // при смене режима игры очищаем поля
                cleanAll();
                // ToDo доработать переключение между режимом игры через интернет и остальными режимами
                // если выбрана игра через интернет - инициализируем FireBase
                if (mGameMode == 4) {
                    fireBaseInit();
                }
                else {
                    // если выбран offLine тип игры
                    // записываем в FireBase сообщение "offLine"
                    sendOfflineMessage();
                    // обнудяем состояния, полученые в сетевой игре
                    mIsPlayerOne = false;
                    mIsPlayerTwo = false;
                    mIsAssign = false;
                    // устанавливаем надпись "offLine"
                    onLineTextView.setText(OFFLINE);
                }

            }
        });

        // нажатие на кнопку "Cancel"
        gameModeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // возвращаем текущее значение GameMode
                mGameMode = tempGameMode;
            }
        });

        // показываем диалоговое окно
        gameModeDialog.show();
    }

    private void sendOfflineMessage() {
        // записываем в FireBase сообщение "offLine"
        if (mIsPlayerOne)
            writeMessage(PLAYER_1 + OFFLINE);
        if (mIsPlayerTwo)
            writeMessage(PLAYER_2 + OFFLINE);
    }

    /**
     * Первичная инициализация массива полей
     *
     * @return двумерный массив кнопок (полей)
     */
    private Button[][] fieldsInit() {
        Button sq0_0 = findViewById(R.id.sq_0_0);
        sq0_0.setOnClickListener(onClickListener);

        Button sq0_1 = findViewById(R.id.sq_0_1);
        sq0_1.setOnClickListener(onClickListener);

        Button sq0_2 = findViewById(R.id.sq_0_2);
        sq0_2.setOnClickListener(onClickListener);

        Button sq1_0 = findViewById(R.id.sq_1_0);
        sq1_0.setOnClickListener(onClickListener);

        Button sq1_1 = findViewById(R.id.sq_1_1);
        sq1_1.setOnClickListener(onClickListener);

        Button sq1_2 = findViewById(R.id.sq_1_2);
        sq1_2.setOnClickListener(onClickListener);

        Button sq2_0 = findViewById(R.id.sq_2_0);
        sq2_0.setOnClickListener(onClickListener);

        Button sq2_1 = findViewById(R.id.sq_2_1);
        sq2_1.setOnClickListener(onClickListener);

        Button sq2_2 = findViewById(R.id.sq_2_2);
        sq2_2.setOnClickListener(onClickListener);

        Button[][] fields = {{sq0_0, sq0_1, sq0_2}, {sq1_0, sq1_1, sq1_2}, {sq2_0, sq2_1, sq2_2}};
        
        return fields;
    }

    /**
     * Инициализация данных для игроков
     */
    private void playersInit() {
        // задаем знаки для игрока 1 и для игрока 2
        mYourSign = X_SIGN;
        mOpponentSign = O_SIGN;

        // устанавливаем первый занак для Player vs Player
        mSignForPvP = mYourSign;

        // инициализируем имена игроков
        mPlayerOneName = "You";
        mPlayerTwoName = "Opponent";
    }

    /**
     * Установка знака путем нажатия игроком на одно из полей
     */
    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Player vs PC
            if (mGameMode == 0) {
                // устанавливаем знак на выбранное поле
                setSignByTouch(v, mYourSign);
                // если победа одного из игроков, либо нет свободных полей - выходим из метода
                if (evaluation())
                    return;
                // ход компьютера
                PC_turn(mOpponentSign);
                // проверяем на победу и на наличие свободных полей
                evaluation();
            }
            // Player vs Player
            else if (mGameMode == 2) {
                // устанавливаем знак на выбранное поле
                setSignByTouch(v, mSignForPvP);
                // если победа одного из игроков, либо нет свободных полей - выходим из метода,
                // устанавливаем знак для первого хода как "Х" по-умолчанию
                if (evaluation()) {
                    mSignForPvP = mYourSign;
                    return;
                }
                // меняем знак для хода
                if (mSignForPvP.equals(mYourSign))
                    mSignForPvP = mOpponentSign;
                else
                    mSignForPvP = mYourSign;
            }
            // network game
            else if (mGameMode == 4) {
                // если есть разрешение на ход
                if (mPermissionToTurn) {
                    // устанавливаем знак на выбранное поле
                    setSignByTouch(v, mYourSign);
                    // запрещаем игроку делать ход
                    mPermissionToTurn = false;
                    // очищаем текстовое поле от Your Turn или Opponent Turns
                    resultTextView.setText("");
                    // если победа одного из игроков, либо нет свободных полей - выходим из метода
                    if (evaluation())
                        return;
                }
            }
        }
    };

    /**
     * Установка знака на выбранное поле
     *
     * @param v    нажатое поле
     * @param sign знак для установки
     */
    private void setSignByTouch(View v, String sign) {
        // переменные для координат
        int coordinateOne = 0;
        int coordinateTwo = 0;
        switch (v.getId()) {
            case R.id.sq_0_0:
                coordinateOne = 0;
                coordinateTwo = 0;
                break;

            case R.id.sq_0_1:
                coordinateOne = 0;
                coordinateTwo = 1;
                break;

            case R.id.sq_0_2:
                coordinateOne = 0;
                coordinateTwo = 2;
                break;

            case R.id.sq_1_0:
                coordinateOne = 1;
                coordinateTwo = 0;
                break;

            case R.id.sq_1_1:
                coordinateOne = 1;
                coordinateTwo = 1;
                break;

            case R.id.sq_1_2:
                coordinateOne = 1;
                coordinateTwo = 2;
                break;

            case R.id.sq_2_0:
                coordinateOne = 2;
                coordinateTwo = 0;
                break;

            case R.id.sq_2_1:
                coordinateOne = 2;
                coordinateTwo = 1;
                break;

            case R.id.sq_2_2:
                coordinateOne = 2;
                coordinateTwo = 2;
                break;
            default:
                break;
        }
        // устанавливаем знак
        putSign(coordinateOne, coordinateTwo, sign);

        // отправляем координаты (для сетевой игры)
        if (mGameMode == 4) {
            // отправляем координаты
            writeMessage(mPlayerNameForNetwork + COORDINATES + "_" + (coordinateOne +
                    "_" + coordinateTwo));
        }
    }

    /**
     * Оценивает сделанный ход
     *
     * @return true, если победа одного из игроков, либо нет свободных полей
     */
    private boolean evaluation() {
        // считываем текущее состояние полей и записываем их в массив mCurrentState
        mCurrentState = readAllFields(); // массив для хранения текущего состояния поля

        // проверяем на победу, поражение и наличие свободных полей
        int returnIndex = mController.evaluateState(mYourSign, mOpponentSign, mCurrentState);

        // оцениваем полученный результат:
        // если победа одного из игроков, либо нет свободных полей - конец игры
        // если нет - ход разрешение хода другого игрока
        if (actionChooser(returnIndex)) {
            finishGame();
            return true;
        } else
            return false;
    }


    /**
     * Ход компьютера
     *
     * @param signToPut знак для хода
     */
    private void PC_turn(String signToPut) {
        // считываем текущее состояние полей и записываем их в массив mCurrentState
        mCurrentState = readAllFields();
        // вычисляем координаты для хода компьютера
        final int[] pcTurnCoordinates = mController.pcTurn(mYourSign, mOpponentSign, mCurrentState);
        // ставим знак на поле с полученными координатами
        putSign(pcTurnCoordinates[0], pcTurnCoordinates[1], signToPut);
    }


    /**
     * Нажатие на кнопку "Clean".
     *
     * @param view
     */
    public void onClean(View view) {
        // очищаем все поля
        cleanAll();
        // устанавливаем переменную mIsGameFinished в false
        mIsGameFinished = false;
        // если игра по сети - отправляем сообщение об очистке
        if (mGameMode == 4) {
            mPermissionToTurn = false;
            writeMessage(mPlayerNameForNetwork + CLEAN_UP);
        }
    }


    /**
     * Очистка всех полей
     */
    public void cleanAll() {
        for (int i = 0; i < mFields.length; i++) {
            for (int j = 0; j < mFields[i].length; j++) {
                // очищаем все поля
                mFields[i][j].setText(EMPTY_SIGN);
                // восстанавливаем возможность для кнопки быть нажатой
                mFields[i][j].setClickable(true);
            }
        }
        // очищаем текстовое поле
        resultTextView.setText("");
    }


    /**
     * метод выбора действия в зависимости от возвращаемого значения проверки на победу
     *
     * @param returnIndex индекс, возвращаемый по окончанию процедуры проверки на победу
     * @return - true, если победа одного из игроков, либо нет свободных полей
     */
    private boolean actionChooser(int returnIndex) {
        switch (returnIndex) {
            case 1:
                // победа игрока 1
                mPlayerOneScore++;
                playerOneScoreTextView.setText(mPlayerOneName + " - " + mPlayerOneScore);
                resultTextView.setText("You won!");
                return true;
            case 2:
                // победа игрока 2
                mPlayerTwoScore++;
                playerTwoScoreTextView.setText(mPlayerTwoName + " - " + mPlayerTwoScore);
                resultTextView.setText("You lose!");
                return true;
            case 3:
                // нет свободных полей (ничья)
                mDraw++;
                drawTextView.setText("Draw - " + mDraw);
                resultTextView.setText("No Empty Fields!");
                return true;
            case 0:
            default:
                return false;
        }
    }


    /**
     * Считываем текущее состояние всех полей
     *
     * @return двумерный массив текущего состояния полей
     */
    private String[][] readAllFields() {
        String[][] currentState = new String[3][3]; // массив для хранения текущего состояния поля
        // считываем значения со всех полей
        for (int i = 0; i < mFields.length; i++) {
            // записываем текущее состояние в массив currentState
            currentState[i][0] = mFields[i][0].getText().toString();
            currentState[i][1] = mFields[i][1].getText().toString();
            currentState[i][2] = mFields[i][2].getText().toString();
        }
        return currentState;
    }


    /**
     * Конец игры
     */
    private void finishGame() {
        // запрещаем нажатие на поля
        for (int i = 0; i < mFields.length; i++) {
            for (int j = 0; j < mFields[i].length; j++) {
                mFields[i][j].setClickable(false);
            }
        }
        // устанавливаем переменную mIsGameFinished в true
        mIsGameFinished = true;
    }

    /**
     * Ставит знак в поле
     *
     * @param coordinateOne координата 1
     * @param coordinateTwo координата 2
     * @param signToTurn    знак для установки
     */
    private void putSign(int coordinateOne, int coordinateTwo, String signToTurn) {
        // ставим знак в поле
        mFields[coordinateOne][coordinateTwo].setText(signToTurn);
        // запрещаем нажатие на поле, в которое пошел компьютер
        mFields[coordinateOne][coordinateTwo].setClickable(false);
    }


    /**
     * Игра PC vs PC
     *
     * @param view
     */
    public void onPCvsPC(View view) {
        if (mGameMode == 1) {
            // если игра не закончена - ход первого PC
            if (!mIsGameFinished) {
                // ход первого PC
                PC_turn(mYourSign);
                // если победа одного из PC, либо нет свободных клеток - выходми из метода
                if (evaluation())
                    return;
                // ход второго РС
                PC_turn(mOpponentSign);
                // проверка на победу либо на наличие свободных полей
                evaluation();
            }
        }

        // игра по сети
        if (mGameMode == 4) {
            // устанавливаем начальное значение в FireBase
            writeMessage(STAND_BY);
        }
    }


    /**
     * Игра через Internet
     * @param receivedMessage Сообщение, полученное из FireBase
     */
    private void playOverNetwork(String receivedMessage) {
        // распределяем роли игроков
        // если роль игрока не назначена
        if (!mIsAssign) {
            if (receivedMessage.equals(REQUEST_SENT)) {
                mIsPlayerTwo = true;
                // назначаем знаки для игроков
                mYourSign = O_SIGN;
                mOpponentSign = X_SIGN;
                mPlayerNameForNetwork = PLAYER_2;
                // отправляем сообщение о получении запроса
                writeMessage(REQUEST_RECEIVED);
            } else {
                mIsPlayerOne = true;
                // назначаем знаки для игроков
                mYourSign = X_SIGN;
                mOpponentSign = O_SIGN;
                mPlayerNameForNetwork = PLAYER_1;
                // отправляем сообщение о поиске оппонента
                writeMessage(REQUEST_SENT);
                onLineTextView.setText(SEARCHING);
            }
            mIsAssign = true;
            return;
        }

        // основной код
        if (mIsAssign) {
            // логика игрока 1
            if (mIsPlayerOne) {
                if (receivedMessage.equals(REQUEST_RECEIVED)) {
                    writeMessage(READY_TO_START);
                    onLineTextView.setText(ONLINE);
                }

                // если получаем ожидание хода от второго игрока, разрешаем ход игроку 1
                if (receivedMessage.equals(PLAYER_2 + WAITING_FOR_THE_TURN)) {
                    mPermissionToTurn = true;
                    if (!mIsGameFinished) {
                        // устанавливаем надпись Your Turn
                        resultTextView.setText(YOUR_TURN);
                    }

                }

                // если второй игрок получил ход, ждем хода от второго игрока
                if (receivedMessage.equals(PLAYER_2 + TURN_RECEIVED)) {
                    writeMessage(PLAYER_1 + WAITING_FOR_THE_TURN);
                    if (!mIsGameFinished) {
                        // устанавливаем надпись Opponent Turns
                        resultTextView.setText(OPPONENT_TURNS);
                    }
                }

                // если отправлено сообщение с координатами
                if (receivedMessage.contains(PLAYER_2 + COORDINATES)) {
                    // разбиваем полученное сообщение на части, отсекаем первые два слова
                    // и извлекае из него координаты
                    String[] coordinates = receivedMessage.split("_");
                    int coordinateOne = Integer.parseInt(coordinates[2]);
                    int coordinateTwo = Integer.parseInt(coordinates[3]);

                    // ставим знак согласно полученных координат
                    putSign(coordinateOne, coordinateTwo, mOpponentSign);
                    // уведомляем игрока 2 об успешном получении координат
                    writeMessage(PLAYER_1 + TURN_RECEIVED);
                    // очищаем текстовое поле
                    resultTextView.setText("");
                    // проверяем на победу
                    evaluation();
                }

                // если игрок 2 очистил поле
                if (receivedMessage.equals(PLAYER_2 + CLEAN_UP)) {
                    // очищаем все поля
                    cleanAll();
                    // устанавливаем переменныю mIsGameFinished в false
                    mIsGameFinished = false;
                    mPermissionToTurn = false;
                    writeMessage(READY_TO_START);
                }

                // если второй игрок вышел из игры
                if (receivedMessage.equals(PLAYER_2 + OFFLINE)) {
                    mIsPlayerOne = false;
                    mIsAssign = false;
                    onLineTextView.setText(OFFLINE);
                    cleanAll();
                    writeMessage(STAND_BY);
                }
            }

            // логика игрока 2
            if (mIsPlayerTwo) {
                if (receivedMessage.equals(READY_TO_START) | receivedMessage.equals(PLAYER_1 + TURN_RECEIVED)) {
                    writeMessage(PLAYER_2 + WAITING_FOR_THE_TURN);
                    onLineTextView.setText(ONLINE);
                    if (!mIsGameFinished) {
                        resultTextView.setText(OPPONENT_TURNS);
                    }
                }

                // если отправлено сообщение с координатами
                if (receivedMessage.contains(PLAYER_1 + COORDINATES)) {
                    // разбиваем полученное сообщение на части и извлекае из него координаты
                    String[] coordinates = receivedMessage.split("_");
                    int coordinateOne = Integer.parseInt(coordinates[2]);
                    int coordinateTwo = Integer.parseInt(coordinates[3]);

                    // ставим знак согласно полученных координат
                    putSign(coordinateOne, coordinateTwo, mOpponentSign);
                    // уведомляем игрока 2 об успешном получении координат
                    writeMessage(PLAYER_2 + TURN_RECEIVED);
                    // очищаем текстовое поле
                    resultTextView.setText("");
                    // проверяем на победу
                    evaluation();
                }

                if (receivedMessage.equals(PLAYER_1 + WAITING_FOR_THE_TURN)) {
                    mPermissionToTurn = true;
                    if (!mIsGameFinished) {
                        resultTextView.setText(YOUR_TURN);
                    }
                }

                // если игрок 1 очистил поле
                if (receivedMessage.equals(PLAYER_1 + CLEAN_UP)) {
                    // очищаем все поля
                    cleanAll();
                    // устанавливаем переменную mIsGameFinished в false
                    mIsGameFinished = false;
                    mPermissionToTurn = false;
                    writeMessage(READY_TO_START);
                }

                // если первый игрок вышел из игры
                if (receivedMessage.equals(PLAYER_1 + OFFLINE)) {
                    mIsPlayerTwo = false;
                    mIsAssign = false;
                    onLineTextView.setText(OFFLINE);
                    cleanAll();
                    writeMessage(STAND_BY);
                }
            }
        }
    }

    // чтение сообщения из EditText
    private String readMessage() {
        return textToTest_EditText.getText().toString();
    }

    // запись сообщения
    private void writeMessage(String message) {
        // записываем в FireBase
        myRef.setValue(message);

        // записываем в EditText (для тестирования)
        textToTest_EditText.setText(message);
    }

    // метод для создания Toast сообщений
    public static void makeToast(Context context, String textToShow) {
        Toast.makeText(context, textToShow, Toast.LENGTH_SHORT).show();
    }


    // работа с FireBase
    private void fireBaseInit() {
        // инициализация базы данных
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("message");

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // если выбран режим игры по сети
                if (mGameMode == 4) {
                    // получение значения из FireBase
                    String value = dataSnapshot.getValue(String.class);
                    // вызываем метод для игры по сети
                    playOverNetwork(value);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
