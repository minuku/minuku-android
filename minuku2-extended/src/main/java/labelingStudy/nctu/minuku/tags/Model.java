package labelingStudy.nctu.minuku.tags;

import com.firebase.client.Firebase;
import com.google.common.collect.EvictingQueue;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import labelingStudy.nctu.minuku.logger.Log;

/**
 * Created by neerajkumar on 10/2/16.
 */

public class Model {
    private static Model instance = null;

    private static int TOP_N = 5;

    private Map<String, Long> tagCountMap;
    private EvictingQueue<String> mostRecentTags = EvictingQueue.create(TOP_N);
    private Firebase mFirebaseRef;
    private Firebase mFirebaseRefForRecentTags;

    private String[] defaultTags = new String[] {"ateout", "junkfood", "exercise", "stress",
            "idk", "guesstimate", "missedbolus", "sitechange", "holiday", "feelinghigh",
            "feelinglow"};

    String TAG = "Model";

    private Model() {
        /* TODO its never used before.
        mFirebaseRef = new Firebase(Constants.FIREBASE_URL_TAG)
                            .child(UserPreferences
                                    .getInstance()
                                    .getPreference(Constants.KEY_ENCODED_EMAIL));
        mFirebaseRefForRecentTags = new Firebase(Constants.FIREBASE_URL_TAG_RECENT)
                .child(UserPreferences
                        .getInstance()
                        .getPreference(Constants.KEY_ENCODED_EMAIL));

        tagCountMap = new HashMap<>();

        // Attempt to get info from DAO here.
        mFirebaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    if(tagCountMap == null) {
                        tagCountMap = new HashMap<>();
                    }
                } else {
                    tagCountMap = (HashMap<String, Long>) dataSnapshot.getValue();
                }

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        mFirebaseRefForRecentTags.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    GenericTypeIndicator<List<String>> t = new GenericTypeIndicator<List<String>>() {};
                    List<String> recentTagsFromFirebase = dataSnapshot.getValue(t);
                    mostRecentTags.addAll(recentTagsFromFirebase);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        */
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public void incrementTagCount(String tag) {
        if(!tagCountMap.containsKey(tag)) {
            tagCountMap.put(tag, 0L);
        }
        mostRecentTags.add(tag);
        tagCountMap.put(tag, tagCountMap.get(tag) + 1);

        // Update database
        mFirebaseRef.setValue(tagCountMap);
        mFirebaseRefForRecentTags.setValue(mostRecentTags);
    }

    public String[] getTags(){
        String[] tags = new String[tagCountMap.keySet().size()];
        return tagCountMap.keySet().toArray(tags);
    }

    public String[] getRelevantTags() {
        String[] mostRelevantTags = new String[TOP_N * 2];
        Set<String> mostRelevantTagsSet = new TreeSet<>();
        //mostRelevantTagsSet.addAll(getMostRecentTags());
        //mostRelevantTagsSet.addAll(tagsSortedByUsage());
        List<String> recentTags = getMostRecentTags();
        List<String> tagsSortedByUsage = tagsSortedByUsage();
        for (int i = 0; i < Math.min(TOP_N, recentTags.size()); i++) {
            mostRelevantTagsSet.add(recentTags.get(i));
        }
        for(int i = 0; i < Math.min(TOP_N, tagsSortedByUsage.size()); i++) {
            mostRelevantTagsSet.add(tagsSortedByUsage.get(i));
        }
        
        Log.d(TAG, "most relevant tag number: " + mostRelevantTags.length);
        Log.d(TAG, "most relevant tag set number: " + mostRelevantTagsSet.size());

        //print tags most relevant
        for(String tag: mostRelevantTagsSet) {
            Log.d(TAG, "~:~ " + tag);
        }

        // Add default tags if we don't have enough tags to show.
        // Doing it one by one as we are adding to a set and there
        // might be times when a defaultTag is also a most recent or
        // most used tag and adding that won't affect the size of the
        // set.
        for(String tag: defaultTags) {
            if(mostRelevantTagsSet.size() >= TOP_N * 2) {
                break;
            }
            mostRelevantTagsSet.add(tag);
        }

        mostRelevantTagsSet.toArray(mostRelevantTags);

        //print tags most relevant
        for(String tag: mostRelevantTagsSet) {
            Log.d(TAG, "~~ " + tag);
        }

        //print tags most relevant
        for(String tag: mostRelevantTags) {
            Log.d(TAG, ":: " + tag);
        }
        //print most recent tags
        for(String tag: getMostRecentTags()) {
            Log.d(TAG, "--" + tag);
        }
        //print most used tags
        for(String tag: tagsSortedByUsage()) {
            Log.d(TAG, "-->" + tag);
        }
        return mostRelevantTags;
    }


    private  List<String> tagsSortedByUsage() {
        List<Map.Entry<String, Long>> list = new LinkedList<>(tagCountMap.entrySet());
        List<String> result = new LinkedList<>();
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                Long i1 = ((Map.Entry<String, Long>) (o1)).getValue();
                Long i2 = ((Map.Entry<String, Long>) (o2)).getValue();
                return i1.compareTo(i2);
            }
        });

        for(Map.Entry<String, Long> entry: list) {
            result.add(entry.getKey());
        }
        return result;
    }

    private List<String> getMostRecentTags() {
        List<String> recentTags = new LinkedList<>();
        recentTags.addAll(mostRecentTags);
        return recentTags;
    }

}
