package ir.msdehghan.revrec.framework.loader;

import ir.msdehghan.revrec.framework.model.Review;

import java.io.IOException;
import java.util.List;

public interface DataLoader {
    List<Review> load() throws IOException;
}
