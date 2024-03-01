package ir.msdehghan.revrec.framework.recommendation.fcg.graph;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FileCoOccurrence {
    public final String source;
    public final String target;

    public FileCoOccurrence(@NotNull String source, @NotNull String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileCoOccurrence that = (FileCoOccurrence) o;
        return (source.equals(that.source) && target.equals(that.target)) ||
                (target.equals(that.source) && source.equals(that.target));
    }

    @Override
    public String toString() {
        return "FileCoOccurrence{" +
                "source='" + source + '\'' +
                ", target='" + target + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        if (source.compareTo(target) < 0) {
            return Objects.hash(source, target);
        } else {
            return Objects.hash(target, source);
        }
    }
}
