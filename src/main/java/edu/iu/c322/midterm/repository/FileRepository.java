package edu.iu.c322.midterm.repository;

import edu.iu.c322.midterm.model.Question;
import edu.iu.c322.midterm.model.Quiz;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class FileRepository {
    private String IMAGES_FOLDER_PATH = "quizzes/questions/images";
    private static final String NEW_LINE = System.lineSeparator();
    private static final String QUESTION_DATABASE_NAME = "quizzes/questions.txt";
    private static final String QUIZ_DATABASE_NAME = "quizzes/quizzes.txt";

    public FileRepository() {
        File imagesDirectory = new File(IMAGES_FOLDER_PATH);
        if(!imagesDirectory.exists()) {
            imagesDirectory.mkdirs();
        }
    }

    private static void appendToFile(Path path, String content)
            throws IOException {
        Files.write(path,
                content.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND);
    }
    private static void clearFile(Path path)
            throws IOException {
        Files.write(path,
                "".getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.WRITE);
    }

    public int add(Question question) throws IOException {
        Path path = Paths.get(QUESTION_DATABASE_NAME);
        List<Question> questions = findAllQuestions();
        int id = 0;
        for(Question q : questions) {
            if(q.getId() > id) {
                id = q.getId();
            }
        }
        id = id + 1;
        question.setId(id);
        String data = question.toLine();
        appendToFile(path, data + NEW_LINE);
        return id;
    }

    // From POST /quizzes - adds a quiz to db

    public int addQuiz(Quiz quiz) throws IOException {
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        List<Quiz> quizzes = findAllQuizzes(); //needs to return list of quizzes
        int id = 0;
        for(Quiz q : quizzes) {
            if(q.getId() > id) {
                id = q.getId();
            }
        }
        id = id + 1;
        quiz.setId(id);
        String data = quiz.toLine(quiz.getId());
        appendToFile(path, data + NEW_LINE);
        return id;
    }

    /*public int removeQuiz(Quiz quiz) throws IOException {
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        List<Quiz> quizzes = findAllQuizzes(); //needs to return list of quizzes
        for (Quiz q : quizzes) {
            if (q.getId() == quiz.getId()){

            }
        }

    }*/





    public List<Question> findAllQuestions() throws IOException {
        List<Question> result = new ArrayList<>();
        Path path = Paths.get(QUESTION_DATABASE_NAME);
        if (Files.exists(path)) {
            List<String> data = Files.readAllLines(path);
            for (String line : data) {
                if(line.trim().length() != 0) {
                    Question q = Question.fromLine(line);
                    result.add(q);
                }
            }
        }
        return result;
    }

    // from POST /quizzes - finds all quizzes to add on to
    public List<Quiz> findAllQuizzes() throws IOException {
        List<Quiz> result = new ArrayList<>();
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        if (Files.exists(path)) {
            List<String> data = Files.readAllLines(path);
            for (String line : data) {
                if(line.trim().length() != 0) {
                    Quiz q = Quiz.fromLine(line);
                    result.add(q);
                }
            }
        }
        return result;
    }






    public List<Question> find(String answer) throws IOException {
        List<Question> animals = findAllQuestions();
        List<Question> result = new ArrayList<>();
        for (Question question : animals) {
            if (answer != null && !question.getAnswer().trim().equalsIgnoreCase(answer.trim())) {
                continue;
            }
            result.add(question);
        }
        return result;
    }

    public List<Question> find(List<Integer> ids) throws IOException {
        List<Question> questions = findAllQuestions();
        List<Question> result = new ArrayList<>();
        for (int id : ids) {
            Question q = questions.stream().filter(x -> x.getId() == id).toList().get(0);
            result.add(q);
        }
        return result;
    }



    public Question get(Integer id) throws IOException {
        List<Question> questions = findAllQuestions();
        for (Question question : questions) {
            if (question.getId() == id) {
                return question;
            }
        }
        return null;
    }

    public boolean updateQuiz(int id, String title, List<Integer> questionIds) throws IOException {
        Path path = Paths.get(QUIZ_DATABASE_NAME);
        //Quiz quiz = getQuiz(id);
        List<Quiz> allQuizzes = findAllQuizzes();

        clearFile(path);
        for (Quiz q : allQuizzes){

            if (q.getId() == id){
                if (title != null){
                    q.setTitle(title);
                }
                if (questionIds != null){
                    q.setQuestionIds(questionIds);
                }
            }
            String data = q.toLine(q.getId());
            appendToFile(path, data + NEW_LINE);
        }

        return true;
    }

    public Quiz getQuiz(Integer id) throws IOException {
        List<Quiz> quizzes = findAllQuizzes();
        Quiz result = null;
        for (Quiz quiz : quizzes) {
            if (quiz.getId() == id) {
                result = quiz;
            }
        }

        if (result != null) {
            List<Question> questions = find(result.getQuestionIds());
            result.setQuestions(questions);
            return result;
        }

        return null;
    }

    public boolean updateImage(int id, MultipartFile file) throws IOException {
        System.out.println(file.getOriginalFilename());
        System.out.println(file.getContentType());

        String fileExtension = ".png";
        Path path = Paths.get(IMAGES_FOLDER_PATH
                + "/" + id + fileExtension);
        System.out.println("The file " + path + " was saved successfully.");
        file.transferTo(path);
        return true;
    }

    public byte[] getImage(int id) throws IOException {
        String fileExtension = ".png";
        Path path = Paths.get(IMAGES_FOLDER_PATH
                + "/" + id + fileExtension);
        byte[] image = Files.readAllBytes(path);
        return image;
    }


}
