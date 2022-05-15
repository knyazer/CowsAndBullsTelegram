package tech.knyaz.cowsandbullstelegram;

import lombok.Getter;

public class Checker {

    @Getter
    private int misplaced;

    @Getter
    private int correct;

    Checker(String obtained, String target)
    {
        if (obtained.length() != target.length())
            throw new IllegalArgumentException("Different size of strings passed to checker");

        correct = 0;
        for (int i = 0; i < obtained.length(); i++)
            if (obtained.charAt(i) == target.charAt(i))
                correct++;

        misplaced = 0;
        for (int i = 0; i < obtained.length(); i++)
            for (int j = 0; j < target.length(); j++)
                if (    obtained.charAt(i) != target.charAt(i) &&
                        obtained.charAt(j) != target.charAt(j) &&
                        i != j &&
                        obtained.charAt(i) == target.charAt(j)) {
                    misplaced++;
                    break;
                }
    }

    public String asString()
    {
        return String.valueOf(misplaced) + " misplaced and " + String.valueOf(correct) + " correct.";
    }
}
