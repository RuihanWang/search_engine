package transform;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class to transform a list of tokens based on provided TokenTransformers.
 *
 */
public class TokenListTransformer {

    /**
     * Apply each of the given TokenTransformers to the each of the tokens in the given list.
     * If a token is equal to the empty String after any of the transformations, then discard
     * the token.
     * @param tokens The list of tokens to be transformed
     * @param transformers The list of TokenTransformers to be applied to each of the tokens
     * @return The list of transformed tokens
     */
    public static List<String> transformTokenList(List<String> tokens, List<TokenTransformer> transformers) {
        List<String> transformedTokens = new ArrayList<String>();
        // look through the tokens and apply each of the TokenTransformers to the tokens
        for (String token : tokens) {
            for (TokenTransformer transformer : transformers) {
                // stop transforming the token if it is the empty string
                if (token.isEmpty()) {
                    break;
                }
                // apply the transformation
                token = transformer.transform(token);
            }
            // discard this token if it is the empty string
            if (token.isEmpty()) {
                continue;
            }
            // add the transformed token to the list of results
            transformedTokens.add(token);
        }
        return transformedTokens;
    }
}
