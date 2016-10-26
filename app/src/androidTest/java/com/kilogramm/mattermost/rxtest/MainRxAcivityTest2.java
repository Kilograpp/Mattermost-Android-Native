package com.kilogramm.mattermost.rxtest;


import android.os.SystemClock;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import com.kilogramm.mattermost.R;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerActions.close;
import static android.support.test.espresso.contrib.DrawerActions.open;
import static android.support.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.view.Gravity.END;
import static com.kilogramm.mattermost.rxtest.TestUtils.atPosition;
import static com.kilogramm.mattermost.rxtest.TestUtils.childAtPosition;
import static com.kilogramm.mattermost.rxtest.TestUtils.clearDataBaseAfterLogout;
import static com.kilogramm.mattermost.rxtest.TestUtils.clearPreference;
import static com.kilogramm.mattermost.rxtest.TestUtils.hasTextInputLayoutErrorText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MainRxAcivityTest2 {

    @Rule
    public ActivityTestRule<MainRxAcivity> mActivityTestRule = new ActivityTestRule<>(MainRxAcivity.class);

    @BeforeClass
    public static void clear(){
        clearPreference();
        clearDataBaseAfterLogout();
    }

    @Test
    public void sendMeesage() {

        // region login
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonNext), withText("Next step")));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("mattertest2@kilograpp.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("123456a"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonNext), withText("Sign in"),
                        withParent(withId(R.id.linearLayout))));
        appCompatButton2.perform(scrollTo(), click());

        onView(allOf(withId(R.id.drawer_layout), isDisplayed())).perform(open(END));

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.profile), isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.email), withText("mattertest2@kilograpp.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("mattertest2@kilograpp.com")));

        ViewInteraction appCompatImageButton = onView(
                allOf(withContentDescription("Перейти вверх"),
                        withParent(allOf(withId(R.id.toolbar),
                                withParent(withId(R.id.layout_app_bar)))),
                        isDisplayed()));
        appCompatImageButton.perform(click());

        //endregion

        onView(allOf(withId(R.id.drawer_layout), isDisplayed())).perform(close(END));

        onView(allOf(withId(R.id.drawer_layout), isDisplayed())).perform(open());

        ViewInteraction recyclerView = onView(
                allOf(withId(R.id.recView), isDisplayed()));
        recyclerView.perform(actionOnItemAtPosition(1, click()));

        ViewInteraction appCompatEditText3 = onView(
                allOf(withId(R.id.writingMessage),
                        withParent(allOf(withId(R.id.sendingMessageContainer),
                                withParent(withId(R.id.newMessageLayout)))),
                        isDisplayed()));
        appCompatEditText3.perform(replaceText("AutoTestAndroid"), closeSoftKeyboard());

        ViewInteraction appCompatTextView = onView(
                allOf(withId(R.id.btnSend), withText("Send"),
                        withParent(allOf(withId(R.id.sendingMessageContainer),
                                withParent(withId(R.id.newMessageLayout)))),
                        isDisplayed()));
        appCompatTextView.perform(click());

        SystemClock.sleep(5000);

        final int[] numberOfAdapterItems = new int[1];
        onView(withId(R.id.rev)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                RecyclerView listView = (RecyclerView) view;

                //here we assume the adapter has been fully loaded already
                numberOfAdapterItems[0] = listView.getAdapter().getItemCount();
                return true;
            }

            @Override
            public void describeTo(Description description) {

            }
        }));

        onView(withId(R.id.rev))
                .perform(scrollToPosition(numberOfAdapterItems[0]-1))
                .check(matches(atPosition(numberOfAdapterItems[0]-1, withText("mattertest2"))));

    }

    @Test
    public void logout(){
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonNext), withText("Next step")));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("mattertest2@kilograpp.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("123456a"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonNext), withText("Sign in"),
                        withParent(withId(R.id.linearLayout))));
        appCompatButton2.perform(scrollTo(), click());

        onView(allOf(withId(R.id.drawer_layout), isDisplayed())).perform(open(END));

        onView(allOf(withId(R.id.nav_view), isDisplayed()))
                .perform(NavigationViewActions.navigateTo(R.id.logout));

        SystemClock.sleep(2000);

        onView(allOf(withId(R.id.buttonNext), withText("Next step"), isDisplayed()));
    }

    @Test
    public void correctlogin(){
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonNext), withText("Next step")));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("mattertest2@kilograpp.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("123456a"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonNext), withText("Sign in"),
                        withParent(withId(R.id.linearLayout))));
        appCompatButton2.perform(scrollTo(), click());

        onView(allOf(withId(R.id.drawer_layout), isDisplayed())).perform(open(END));

        ViewInteraction linearLayout = onView(
                allOf(withId(R.id.profile), isDisplayed()));
        linearLayout.perform(click());

        ViewInteraction textView = onView(
                allOf(withId(R.id.email), withText("mattertest2@kilograpp.com"),
                        childAtPosition(
                                childAtPosition(
                                        IsInstanceOf.<View>instanceOf(android.widget.FrameLayout.class),
                                        0),
                                1),
                        isDisplayed()));
        textView.check(matches(withText("mattertest2@kilograpp.com")));
    }

    @Test
    public void incorrectLogin(){
        ViewInteraction appCompatButton = onView(
                allOf(withId(R.id.buttonNext), withText("Next step")));
        appCompatButton.perform(scrollTo(), click());

        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.editEmail), isDisplayed()));
        appCompatEditText.perform(replaceText("mattertestww2@kilograpp.com"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.editPassword), isDisplayed()));
        appCompatEditText2.perform(replaceText("123456a"), closeSoftKeyboard());

        ViewInteraction appCompatButton2 = onView(
                allOf(withId(R.id.buttonNext), withText("Sign in"),
                        withParent(withId(R.id.linearLayout))));
        appCompatButton2.perform(scrollTo(), click());

        onView(withText("We couldn't find an existing account matching your credentials. This team may require an invite from the team owner to join."))
                .inRoot(withDecorView(not(is(mActivityTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));
    }

   @Test
    public void checkValidUrl(){

       ViewInteraction appCompatEditText = onView(
               allOf(withId(R.id.urlEditText), isDisplayed()));
       appCompatEditText.perform(replaceText("mattertestww.kilograpp.c"), closeSoftKeyboard());

       ViewInteraction appCompatButton = onView(
               allOf(withId(R.id.buttonNext), withText("Next step")));
       appCompatButton.perform(scrollTo(), click());

       onView(allOf(withId(R.id.editTextInputLayout), isDisplayed()))
               .check(matches(hasTextInputLayoutErrorText("Url is not valid https://")));
   }


    @After
    public  void clearDB(){
        clearPreference();
        clearDataBaseAfterLogout();
    }

}
