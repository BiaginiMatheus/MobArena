package com.garbagemule.MobArena.signs;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@SuppressWarnings("WeakerAccess")
@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class HandlesSignClicksTest {

    SignStore signStore;
    InvokesSignAction invokesSignAction;

    HandlesSignClicks subject;

    @Before
    public void setup() {
        signStore = mock(SignStore.class);
        invokesSignAction = mock(InvokesSignAction.class);

        subject = new HandlesSignClicks(signStore, invokesSignAction);
    }

    @Test
    public void noBlockNoFun() {
        PlayerInteractEvent event = event(null, null);

        subject.on(event);

        verifyNoInteractions(signStore, invokesSignAction);
    }

    @Test
    public void noSignBlockNoFun() {
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(mock(Chest.class));
        PlayerInteractEvent event = event(null, block);

        subject.on(event);

        verifyNoInteractions(signStore, invokesSignAction);
    }

    @Test
    public void sneakingPlayerNoFun() {
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(mock(Sign.class));
        Player player = mock(Player.class);
        when(player.isSneaking()).thenReturn(true);
        PlayerInteractEvent event = event(player, block);

        subject.on(event);

        verifyNoInteractions(signStore, invokesSignAction);
    }

    @Test
    public void nonArenaSignNoFun() {
        Block block = mock(Block.class);
        when(block.getState()).thenReturn(mock(Sign.class));
        Player player = mock(Player.class);
        when(player.isSneaking()).thenReturn(false);
        when(signStore.findByLocation(any())).thenReturn(null);
        PlayerInteractEvent event = event(player, block);

        subject.on(event);

        verifyNoInteractions(invokesSignAction);
    }

    @Test
    public void arenaSignInvokesAction() {
        Location location = mock(Location.class);
        Block block = mock(Block.class);
        when(block.getLocation()).thenReturn(location);
        when(block.getState()).thenReturn(mock(Sign.class));
        Player player = mock(Player.class);
        when(player.isSneaking()).thenReturn(false);
        ArenaSign sign = new ArenaSign(location, "", "", "");
        when(signStore.findByLocation(location)).thenReturn(sign);
        PlayerInteractEvent event = event(player, block);

        subject.on(event);

        verify(invokesSignAction).invoke(sign, player);
    }

    private PlayerInteractEvent event(Player player, Block block) {
        Action action = Action.LEFT_CLICK_BLOCK;
        return new PlayerInteractEvent(player, action, null, block, null);
    }

}
