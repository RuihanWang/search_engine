package transform;

/**
 * A class used to transform a token by case folding the given token to lowercase.
 *
 */
public class CaseFolder implements TokenTransformer {

    /**
     * Case fold the given token to lowercase letters
     * @param token A String token that will be case folded
     * @return A lowercase String token based on the input token
     */
    public String transform(String token) {
        return token.toLowerCase();
    }
}
