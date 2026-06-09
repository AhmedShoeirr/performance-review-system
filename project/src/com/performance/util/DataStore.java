package com.performance.util;

import com.performance.model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataStore {
    private static DataStore instance;
    private List<User> users;
    private List<Review> reviews;
    private List<TrainingRecommendation> trainingRecommendations;
    private List<UpwardReview> upwardReviews;
    private List<Goal> goals;
    private List<String> availableCourses;
    private List<PendingAction> pendingActions;
    private List<PeerReview> peerReviews;
    private static final String DATA_FILE = "performance_data.ser";
    public static int MAX_TIERS = 3;

    private DataStore() {
        users = new ArrayList<>();
        reviews = new ArrayList<>();
        trainingRecommendations = new ArrayList<>();
        upwardReviews = new ArrayList<>();
        goals = new ArrayList<>();
        availableCourses = new ArrayList<>();
        pendingActions = new ArrayList<>();
        peerReviews = new ArrayList<>();
        loadData();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                users = (List<User>) ois.readObject();
                reviews = (List<Review>) ois.readObject();
                try {
                    trainingRecommendations = (List<TrainingRecommendation>) ois.readObject();
                    upwardReviews = (List<UpwardReview>) ois.readObject();
                    goals = (List<Goal>) ois.readObject();
                    availableCourses = (List<String>) ois.readObject();
                    MAX_TIERS = ois.readInt();
                } catch (EOFException | OptionalDataException e) {
                    // Handle legacy data files
                    if (trainingRecommendations == null)
                        trainingRecommendations = new ArrayList<>();
                    if (upwardReviews == null)
                        upwardReviews = new ArrayList<>();
                    if (goals == null)
                        goals = new ArrayList<>();
                    if (availableCourses == null)
                        availableCourses = new ArrayList<>();
                    if (pendingActions == null)
                        pendingActions = new ArrayList<>();
                    if (peerReviews == null)
                        peerReviews = new ArrayList<>();
                }
                System.out.println("Data loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading data: " + e.getMessage());
                users = new ArrayList<>();
                reviews = new ArrayList<>();
                trainingRecommendations = new ArrayList<>();
                upwardReviews = new ArrayList<>();
                pendingActions = new ArrayList<>();
                peerReviews = new ArrayList<>();
            }
        } else {
            System.out.println("No data file found. Starting fresh.");
        }
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(users);
            oos.writeObject(reviews);
            oos.writeObject(trainingRecommendations);
            oos.writeObject(upwardReviews);
            oos.writeObject(goals);
            oos.writeObject(availableCourses);
            oos.writeInt(MAX_TIERS);
            oos.writeObject(pendingActions);
            oos.writeObject(peerReviews);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public User authenticate(String username, String password) {
        for (User u : users) {
            if (u.getUsername().equals(username) && u.checkPassword(password)) {
                return u;
            }
        }
        return null;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
        saveData();
    }

    public void removeUser(User user) {
        users.remove(user);
        saveData();
    }

    public void addReview(Review review) {
        reviews.add(review);
        saveData();
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public List<Review> getReviewsForUser(User e) {
        List<Review> result = new ArrayList<>();
        for (Review r : reviews) {
            if (r.getEmployee() != null && r.getEmployee().getUsername().equals(e.getUsername())) {
                result.add(r);
            }
        }
        return result;
    }

    public List<TrainingRecommendation> getTrainingRecommendations() {
        return trainingRecommendations;
    }

    public List<UpwardReview> getUpwardReviews() {
        return upwardReviews;
    }

    public void addTrainingRecommendation(TrainingRecommendation tr) {
        trainingRecommendations.add(tr);
        saveData();
    }

    public void addUpwardReview(UpwardReview ur) {
        upwardReviews.add(ur);
        saveData();
    }

    public List<User> getAllDescendants(User manager) {
        return getAllDescendants(manager, new HashSet<>());
    }

    private List<User> getAllDescendants(User manager, Set<User> visited) {
        List<User> descendants = new ArrayList<>();
        visited.add(manager);
        // Check if user has direct reports (any User can be a manager now)
        if (manager.getDirectReports() != null && !manager.getDirectReports().isEmpty()) {
            for (User e : manager.getDirectReports()) {
                if (!visited.contains(e)) {
                    descendants.add(e);
                    descendants.addAll(getAllDescendants(e, visited));
                }
            }
        }
        return descendants;
    }

    public List<User> getChainOfCommand(User user) {
        List<User> chain = new ArrayList<>();
        Set<User> visited = new HashSet<>();
        User current = user.getManager();
        while (current != null) {
            if (visited.contains(current) || current == user) {
                break; // Cycle detected
            }
            visited.add(current);
            chain.add(current);
            current = current.getManager();
        }
        return chain;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void addGoal(Goal goal) {
        goals.add(goal);
        saveData();
    }

    public List<String> getAvailableCourses() {
        return availableCourses;
    }

    public void addCourse(String course) {
        if (!availableCourses.contains(course)) {
            availableCourses.add(course);
            saveData();
        }
    }

    /**
     * Get team members for a user: direct reports + peers (same tier, same manager)
     */
    public List<User> getTeamMembers(User user) {
        List<User> team = new ArrayList<>();

        // Add direct reports (users from tier-1 who report to this user)
        if (user.getDirectReports() != null) {
            team.addAll(user.getDirectReports());
        }

        // Add peers (users from same tier with same manager)
        User manager = user.getManager();
        int tier = user.getTierLevel();

        for (User u : users) {
            if (u == user)
                continue; // Skip self
            if (u instanceof Admin)
                continue; // Skip admins

            // Check if same tier
            if (u.getTierLevel() == tier) {
                // Check if same manager (both null or same object)
                User uManager = u.getManager();
                if ((manager == null && uManager == null) ||
                        (manager != null && manager.equals(uManager))) {
                    team.add(u);
                }
            }
        }

        return team;
    }

    public void removeCourse(String course) {
        availableCourses.remove(course);
        saveData();
    }

    public void removeTrainingRecommendation(TrainingRecommendation tr) {
        trainingRecommendations.remove(tr);
        saveData();
    }

    public void removeReview(Review review) {
        reviews.remove(review);
        saveData();
    }

    public void removeGoal(Goal goal) {
        goals.remove(goal);
        saveData();
    }

    // ==================== Pending Actions ====================

    public List<PendingAction> getPendingActions() {
        if (pendingActions == null) {
            pendingActions = new ArrayList<>();
        }
        return pendingActions;
    }

    public void addPendingAction(PendingAction action) {
        if (pendingActions == null) {
            pendingActions = new ArrayList<>();
        }
        pendingActions.add(action);
        saveData();
    }

    // ==================== Peer Reviews ====================

    public List<PeerReview> getPeerReviews() {
        if (peerReviews == null) {
            peerReviews = new ArrayList<>();
        }
        return peerReviews;
    }

    public void addPeerReview(PeerReview review) {
        if (peerReviews == null) {
            peerReviews = new ArrayList<>();
        }
        peerReviews.add(review);
        saveData();
    }

    public List<PeerReview> getPeerReviewsFor(User user) {
        List<PeerReview> result = new ArrayList<>();
        if (peerReviews != null && user != null) {
            for (PeerReview pr : peerReviews) {
                if (pr.isAbout(user)) {
                    result.add(pr);
                }
            }
        }
        return result;
    }
}
