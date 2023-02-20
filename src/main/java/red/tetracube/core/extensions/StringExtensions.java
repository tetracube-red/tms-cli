package red.tetracube.core.extensions;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class StringExtensions {

    public static String toSlug(String input) {
        final var nonLatin = Pattern.compile("[^\\w-]");
        final var whiteSpace = Pattern.compile("[\\s]");
        final var noWhitespace = whiteSpace.matcher(input).replaceAll("-");
        final var normalized = Normalizer.normalize(noWhitespace, Normalizer.Form.NFD);
        final var slug = nonLatin.matcher(normalized).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

}
