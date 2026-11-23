using Godot;
using System;

namespace DoraMCU.Core
{
    /// <summary>
    /// Singleton base class for Godot - equivalent to Unity's Singleton pattern.
    /// Use this as a base class for managers that should exist globally.
    /// In Godot, singletons are registered as Autoloads in project.godot.
    /// </summary>
    public abstract partial class Singleton : Node
    {
        public override void _Ready()
        {
            GD.Print($"[Singleton] {GetType().Name} initialized");
        }
    }
}
