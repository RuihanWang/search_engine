package transform;

/**
 * A module to process and transform text tokens into forms
 * that are more appropriate to become index terms.
 *
 * This will most likely include case folding and punctuation removal.
 * Other possible options could be stopping, stemming, etc.
 *
 */
public interface TokenTransformer {

    /**
     * Transform the given String token
     * @param token The String token to be transformed
     * @return A String representing the transformed String token
     * that was given
     */
    String transform(String token);
}
