package polytech.followit;

/**
 * Created by Akme on 23/01/2017.
 */
public class Path {
    private static Path ourInstance = new Path();

    public static Path getInstance() {
        return ourInstance;
    }

    private Path() {
    }
}
