


This file is to keep my notes on adding NBT data to track block types. 


The overall idea is fairly simple. 

1. Create a listener for the shovel right click action
   2. Depending on how this works, you may need to check if the block changed.
3. Save the blocks original name/state as NBT data for the current, presumably, x type path block
4. Read the data to change it back later, i.e. grass->path->grass or dirt->path-> dirt

There are two main reasons to do this
1. This will allow greater mod interoperability without causing other mods to change anything
2. The blocks returning to their previous states/types is more consistent with the direction of the vanilla game


I am not currently sure on how to write the nbt data for the block, which seems to require a custom block entity. I have not had the pleasure of creating this before. My main concern is can this block entity be applied to the block without breaking its other functionality? I also wonder the same of modded blocks. Another big concern is can this be applied to any block or will there need to be one made for every path block type? That would make this not compatible with any mods, but I do not believe this is the case. 


I may have found an easier way by using the in game commands to modify the nbt data and read it. 

so, I believe I just need 
/setblock <x> <y> <z> <block_id> [blockstate] [nbt]
and 
/data get  (need to look into how this command works)

