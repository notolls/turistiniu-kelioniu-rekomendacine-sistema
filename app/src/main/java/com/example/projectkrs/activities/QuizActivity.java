package com.example.projectkrs.activities;

import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectkrs.R;
import com.example.projectkrs.utils.UserBackgroundHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText;
    private RadioGroup answersGroup;
    private Button submitButton;

    private int currentQuestionIndex = 0;
    private List<Question> questions = new ArrayList<>();

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);
        UserBackgroundHelper.applySelectedBackground(this);

        questionText = findViewById(R.id.questionText);
        answersGroup = findViewById(R.id.answersGroup);
        submitButton = findViewById(R.id.submitButton);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadQuestions();
        showQuestion();

        submitButton.setOnClickListener(v -> checkAnswer());
    }

    private void loadQuestions() {

        questions.add(new Question(
                "Kur yra Gedimino pilis?",
                new String[]{"Kaunas", "Vilnius", "Klaipėda", "Šiauliai"},
                1
        ));

        questions.add(new Question(
                "Kuri vieta yra UNESCO paveldas?",
                new String[]{"Trakų pilis", "Akropolis", "Žalgirio arena", "Panorama"},
                0
        ));

        questions.add(new Question(
                "Kokios veiklos labiausiai rekomenduojamos Karoliniškių kraštovaizdžio draustinyje?",
                new String[]{"Pėsčiųjų žygiai ir pasivaikščiojimai", "Maisto gaminimas", "Ledo ritulys", "Automobilių lenktynės"},
                0
        ));

        questions.add(new Question(
                "Kokia aplinka vyrauja draustinyje?",
                new String[]{"Urbanizuota ir triukšminga", "Tyli, gamtinga ir miškinga", "Pramoninė zona", "Smėlio dykuma"},
                1
        ));

        questions.add(new Question(
                "Kur geriausia patirti Karoliniškių kraštovaizdžio draustinį?",
                new String[]{"Važiuojant dviračiu", "Automobiliu per parką", "Pėsčiomis arba bėgiojant", "Tik virtualiai"},
                2
        ));

        questions.add(new Question(
                "Kokios gyvūnijos galima stebėti draustinyje?",
                new String[]{"Žuvys ir antys", "Tigras ir liūtas", "Delfinai", "Pingvinai"},
                0
        ));

        questions.add(new Question(
                "Kaip draustinis palyginamas su netoliese esančiu Vingio parku?",
                new String[]{"Labiau triukšmingas ir lankomas", "Tyli, rami ir mažiau žinoma vieta", "Labai urbanizuota", "Skirta tik sporto renginiams"},
                1
        ));

        questions.add(new Question(
                "Ar draustinyje yra žaidimų aikštelė vaikams?",
                new String[]{"Taip, netoli miško ✅", "Ne, nėra jokios aikštelės", "Tik vienas stalas su kėdėmis", "Tik sporto inventorius"},
                0
        ));

        questions.add(new Question(
                "Kokie vaizdai pasiekiami draustinyje?",
                new String[]{"Kalvos ir miškai su Vilniaus panorama", "Tiesiog miesto gatvės", "Pramonės pastatai", "Jūra"},
                0
        ));

        questions.add(new Question(
                "Kokios veiklos nėra rekomenduojamos draustinyje?",
                new String[]{"Pasivaikščiojimai", "Bėgiojimas", "Važinėjimas dviračiu per mišką", "Automobilių lenktynės ✅"},
                3
        ));

        questions.add(new Question(
                "Kaip apibūdintumėte draustinį pagal komentarus?",
                new String[]{"Triukšmingas ir pilnas žmonių", "Tyli gamta, puiki vieta atsipalaiduoti ✅", "Urbanizuotas ir neįdomus", "Pramoninė zona"},
                1
        ));

        questions.add(new Question(
                "Kokios trasos labiausiai rekomenduojamos draustinyje?",
                new String[]{"Tylūs pėsčiųjų ir žygių takai ✅", "Automobilių lenktynių trasos", "Dviračių greičio trasos", "Slidinėjimo trasos"},
                0
        ));


        questions.add(new Question(
                "Koks įspūdis apie Geležinkelių muziejų pagal Aivaro komentarą?",
                new String[]{"Modernus, bet trūksta turinio ✅", "Pilnas eksponatų ir labai interaktyvus", "Sunkiai pasiekiamas", "Nėra jokių interaktyvių elementų"},
                0
        ));

        questions.add(new Question(
                "Kas labiausiai patinka vaikams Geležinkelių muziejuje?",
                new String[]{"Interaktyvūs mygtukai, šviesos ir geležinkelio simuliatoriai ✅", "Tik senos knygos", "Tik tikros lokomotyvų kabinos be žaidimų", "Ekskursijos tik su audio gidu"},
                0
        ));

        questions.add(new Question(
                "Kaip muziejus pasikeitė po renovacijos?",
                new String[]{"Dabar labiau interaktyvus ir tinkamas vaikams ✅", "Dabar uždarytas ir nebepriima lankytojų", "Sumažėjo eksponatų", "Tapo tik virtualiu muziejumi"},
                0
        ));

        questions.add(new Question(
                "Kokios papildomos patirtys laukia muziejuje, be eksponatų?",
                new String[]{"Simuliatoriai, garsiniai efektai, interaktyvios lentos ✅", "Tik klasikiniai eksponatai", "Tik kavos pertraukos", "Tik stacionarūs ekranai be interaktyvumo"},
                0
        ));

        questions.add(new Question(
                "Ką galima pamatyti muziejaus lauko ekspozicijoje?",
                new String[]{"Senas lokomotyvas ir traukinių vagonai ✅", "Tik parką", "Tik miesto gatves", "Tik statulas"},
                0
        ));

        questions.add(new Question(
                "Koks muziejaus personalas pagal lankytojus?",
                new String[]{"Draugiškas ir paslaugus ✅", "Nepaslaugus ir uždarytas", "Tik automatai", "Tik saugos darbuotojai"},
                0
        ));

        questions.add(new Question(
                "Ką pataria Aliaksandra apie muziejaus elevatorius?",
                new String[]{"Nesidrovėti ir naudotis juo ✅", "Vengti naudoti elevatorių", "Tik laiptais galima patekti", "Elevatorius neveikia"},
                0
        ));

        questions.add(new Question(
                "Kokias technologijas galima išbandyti Geležinkelių muziejuje?",
                new String[]{"VR akiniai ir geležinkelio simuliatoriai ✅", "Tik popierinius maketus", "Tik audio gidas", "Tik video pristatymus"},
                0
        ));

        questions.add(new Question(
                "Kaip muziejus vertinamas suaugusiųjų po renovacijos?",
                new String[]{"Įdomus ir netikėtai įtraukiantis ✅", "Nuobodus ir nenaudingas", "Per daug triukšmingas", "Tik vaikams skirtas"},
                0
        ));

        questions.add(new Question(
                "Kokio trūkumo kartais pastebi lankytojai?",
                new String[]{"Kai kurie VR akiniai neveikia ✅", "Nėra interaktyvių elementų", "Eksponatai netinkami vaikams", "Muziejus nepriima lankytojų"},
                0
        ));


    }

    private void showQuestion() {

        answersGroup.removeAllViews();

        Question q = questions.get(currentQuestionIndex);
        questionText.setText(q.question);

        for (int i = 0; i < q.answers.length; i++) {
            RadioButton rb = new RadioButton(this);
            rb.setText(q.answers[i]);
            rb.setId(i);
            answersGroup.addView(rb);
        }
    }

    private void checkAnswer() {

        int selectedId = answersGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(this, "Pasirink atsakymą!", Toast.LENGTH_SHORT).show();
            return;
        }

        Question q = questions.get(currentQuestionIndex);

        if (selectedId == q.correctAnswerIndex) {

            // 🟢 +10 taškų į Firestore
            db.collection("users")
                    .document(userId)
                    .update("points", FieldValue.increment(10));

            // ✨ Rodom deimanto animaciją
            showDiamondAnimation("+10");
        } else {
            Toast.makeText(this, "Neteisinga 😢", Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;

        if (currentQuestionIndex < questions.size()) {
            showQuestion();
        } else {
            Toast.makeText(this, "Viktorina baigta!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showDiamondAnimation(String text) {
        // 💎 Sukuriam ImageView su deimantu
        ImageView diamond = new ImageView(this);
        diamond.setImageResource(R.drawable.ic_diamond); // sukuri deimanto drawable
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.CENTER;
        addContentView(diamond, params);

        // 🟢 Scale animacija + fade in/out
        ScaleAnimation scale = new ScaleAnimation(
                0f, 1.5f, 0f, 1.5f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scale.setDuration(800);

        AlphaAnimation fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setStartOffset(800);
        fadeOut.setDuration(500);

        diamond.startAnimation(scale);
        diamond.startAnimation(fadeOut);

        // Automatiškai pašalinam po animacijos
        diamond.postDelayed(() -> ((ViewGroup) diamond.getParent()).removeView(diamond), 1300);
    }

    static class Question {
        String question;
        String[] answers;
        int correctAnswerIndex;

        Question(String question, String[] answers, int correctAnswerIndex) {
            this.question = question;
            this.answers = answers;
            this.correctAnswerIndex = correctAnswerIndex;
        }
    }
}